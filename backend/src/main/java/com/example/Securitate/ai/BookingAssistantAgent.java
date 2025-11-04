package com.example.Securitate.ai;

import com.example.Securitate.Cars.Car;
import com.example.Securitate.Cars.CarRepository;
import com.example.Securitate.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class BookingAssistantAgent {

    private final ChatClient.Builder chatClientBuilder;
    private final CarRepository carRepository;
    private final ReservationRepository reservationRepository;

    private static final String SYSTEM_PROMPT = """
        Ești BookingAssistant pentru un serviciu de închirieri auto.
        Dacă utilizatorul cere informații generale, răspunde prietenos, pe scurt.
        Dacă mesajul este despre DB (ex: "găsește o automată sub 200 RON/zi între X și Y"),
        roagă-ți „tool-urile” interne. RETURNAREA de date reale se face din DB, nu inventa răspunsuri.
        """;

    // Formate de dată acceptate în text
    private static final DateTimeFormatter[] DATE_FORMATS = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };

    /* ===================== PUNCT UNIC DE INTRARE ===================== */

    public BookingAssistantController.ChatResponseDTO chat(String sessionId, String userMessage) {
        // 0) Întrebări generale: oferă acțiune confirm_navigate către /cars
        if (isGeneralListQuestion(userMessage)) {
            return new BookingAssistantController.ChatResponseDTO(
                    "Avem o gamă variată de mașini (economice, SUV, lux). Vrei să te duc la pagina cu toate mașinile?",
                    List.of(
                            new BookingAssistantController.ActionDTO(
                                    "confirm_navigate", "/cars", "Da, deschide toate mașinile"
                            ),
                            new BookingAssistantController.ActionDTO(
                                    "dismiss", "", "Nu, rămân în chat"
                            )
                    )
            );
        }

        // 1) Căutări după id=... + (opțional) perioadă
        if (userMessage != null && userMessage.toLowerCase(Locale.ROOT).contains("id=")) {
            String reply = handleById(userMessage);
            return new BookingAssistantController.ChatResponseDTO(reply, List.of());
        }

        // 2) Criterii (brand, buget, cutie, combustibil) + opțional perioadă
        if (mightBeCriteriaSearch(userMessage)) {
            String byCriteria = handleByCriteria(userMessage);
            if (byCriteria != null) {
                return new BookingAssistantController.ChatResponseDTO(byCriteria, List.of());
            }
        }

        // 2.5) Calculează prețul pentru {model} din {data} până pe {data}
        String lower = userMessage == null ? "" : userMessage.toLowerCase(Locale.ROOT);
        boolean isQuoteIntent =
                lower.contains("calculeaza pretul") || lower.contains("calculează prețul") ||
                        (lower.contains("calculeaza") && lower.contains("pret"));

        if (isQuoteIntent) {
            Car car = findCarByMention(userMessage);
            DateRange range = parseDateRange(userMessage);

            if (car == null) {
                return new BookingAssistantController.ChatResponseDTO(
                        "Spune-mi, te rog, modelul exact (ex: „Audi RS6” sau „BMW 520”) ca să pot calcula prețul.",
                        List.of()
                );
            }
            if (range == null) {
                return new BookingAssistantController.ChatResponseDTO(
                        "Dă-mi și intervalul (ex: „din 2025-11-01 până pe 2025-11-05”) ca să calculez prețul.",
                        List.of()
                );
            }

            boolean overlap = reservationRepository.existsOverlap(car.getId(), range.start, range.end);
            String availability = overlap ? "INDISPONIBILĂ" : "DISPONIBILĂ";

            String quote = formatQuote(car, range.start, range.end)
                    + String.format(Locale.ROOT, "\nDisponibilitate: %s", availability);

            return new BookingAssistantController.ChatResponseDTO(
                    quote,
                    List.of(new BookingAssistantController.ActionDTO(
                            "offer_navigate",
                            "/view-car-image/" + car.getId(),
                            "Vezi detalii"
                    ))
            );
        }

        // 3) Fallback: LLM (mini)
        ChatClient chat = chatClientBuilder.build();
        String llmAnswer = chat
                .prompt()
                .system(SYSTEM_PROMPT)
                .user("sessionId=" + sessionId + " | " + userMessage)
                .call()
                .content();

        return new BookingAssistantController.ChatResponseDTO(llmAnswer, List.of());
    }

    /* ====================== BY ID ====================== */

    private String handleById(String text) {
        Long id = extractId(text);
        if (id == null) {
            return "ID invalid sau inexistent în mesaj.";
        }

        var carOpt = carRepository.findById(id);
        if (carOpt.isEmpty()) {
            return "Nu am găsit o mașină cu id=" + id + ".";
        }
        Car car = carOpt.get();

        // verificare disponibilitate dacă avem interval
        var dates = extractDates(text);
        if (dates.size() >= 2) {
            var start = normalize(dates.get(0), true);
            var end   = normalize(dates.get(1), false);
            boolean overlap = reservationRepository.existsOverlap(id, start, end);
            String avail = overlap ? "INDISPONIBILĂ" : "DISPONIBILĂ";
            return formatCarLine(car) + " | Disponibilitate [" + start + " → " + end + "]: " + avail;
        }

        return formatCarLine(car);
    }

    /* ====================== BY CRITERIA ====================== */

    private String handleByCriteria(String text) {
        String brand        = extractBrand(text);
        String transmission = extractTransmission(text);
        String fuel         = extractFuel(text);
        Integer maxPrice    = extractBudget(text);
        Integer exactPrice  = extractExactPrice(text);
        var dates           = extractDates(text);

        LocalDateTime start = dates.size() >= 1 ? normalize(dates.get(0), true) : null;
        LocalDateTime end   = dates.size() >= 2 ? normalize(dates.get(1), false) : null;

        List<Car> all = carRepository.findAll();

        // filtrează pe brand dacă a fost menționat
        if (brand != null) {
            List<Car> onlyBrand = new ArrayList<>();
            for (Car c : all) {
                if (c.getBrand() != null && c.getBrand().equalsIgnoreCase(brand)) {
                    onlyBrand.add(c);
                }
            }
            if (onlyBrand.isEmpty()) {
                return "Nu avem mașini " + brand.toUpperCase(Locale.ROOT) + " în flotă momentan.";
            }
            all = onlyBrand;
        }

        // utilitar pentru disponibilitate
        java.util.function.Predicate<Car> isAvailableInWindow = c -> {
            if (start == null || end == null) return true;
            return !reservationRepository.existsOverlap(c.getId(), start, end);
        };

        // dacă utilizatorul vrea un preț exact (ex: „la 100 ron” / „100 ron/zi”)
        if (exactPrice != null) {
            List<Car> exactMatches = new ArrayList<>();
            for (Car c : all) {
                Integer p = c.getPrice_per_day();
                if (p != null && p.intValue() == exactPrice.intValue()) {
                    if (transmission != null && !containsIgnoreCase(c.getTransmission(), transmission)) continue;
                    if (fuel != null && !containsIgnoreCase(c.getFuel_type(), fuel)) continue;
                    if (!isAvailableInWindow.test(c)) continue;
                    exactMatches.add(c);
                }
            }

            if (!exactMatches.isEmpty()) {
                exactMatches.sort(Comparator.comparing(Car::getModel, Comparator.nullsLast(String::compareToIgnoreCase)));
                StringBuilder sb = new StringBuilder();
                if (start != null && end != null) {
                    sb.append("Mașini la ").append(exactPrice).append(" RON/zi DISPONIBILE în intervalul [")
                            .append(start).append(" → ").append(end).append("]:\n");
                } else {
                    sb.append("Mașini la ").append(exactPrice).append(" RON/zi:\n");
                }
                int limit = Math.min(10, exactMatches.size());
                for (int i = 0; i < limit; i++) {
                    sb.append(" • ").append(formatCarLine(textMode(exactMatches.get(i))));
                    if (i < limit - 1) sb.append("\n");
                }
                if (exactMatches.size() > limit) {
                    sb.append("\n…și alte ").append(exactMatches.size() - limit).append(" opțiuni.");
                }
                return sb.toString();
            }

            // altfel: oferă cele mai apropiate sub/peste exactPrice (disponibile și pe criterii)
            Car bestBelow = null;
            Car bestAbove = null;
            for (Car c : all) {
                Integer p = c.getPrice_per_day();
                if (p == null) continue;
                if (transmission != null && !containsIgnoreCase(c.getTransmission(), transmission)) continue;
                if (fuel != null && !containsIgnoreCase(c.getFuel_type(), fuel)) continue;
                if (!isAvailableInWindow.test(c)) continue;

                if (p < exactPrice) {
                    if (bestBelow == null || p > bestBelow.getPrice_per_day()) bestBelow = c;
                } else if (p > exactPrice) {
                    if (bestAbove == null || p < bestAbove.getPrice_per_day()) bestAbove = c;
                }
            }

            if (bestBelow == null && bestAbove == null) {
                return "Nu am găsit mașini apropiate de " + exactPrice + " RON/zi pentru criteriile date.";
            }

            StringBuilder sb = new StringBuilder("Nu avem exact la ")
                    .append(exactPrice).append(" RON/zi, dar iată alternativele cele mai apropiate");
            if (start != null && end != null) {
                sb.append(" DISPONIBILE în intervalul [").append(start).append(" → ").append(end).append("]");
            }
            sb.append(":\n");

            if (bestBelow != null) sb.append(" (Sub) ").append(formatCarLine(bestBelow)).append("\n");
            if (bestAbove != null) sb.append(" (Peste) ").append(formatCarLine(bestAbove));
            return sb.toString();
        }

        // filtrare generală
        List<Car> filtered = new ArrayList<>();
        for (Car c : all) {
            if (transmission != null && !containsIgnoreCase(c.getTransmission(), transmission)) continue;
            if (fuel != null && !containsIgnoreCase(c.getFuel_type(), fuel)) continue;
            if (maxPrice != null && (c.getPrice_per_day() == null || c.getPrice_per_day() > maxPrice)) continue;
            if (!isAvailableInWindow.test(c)) continue;
            filtered.add(c);
        }

        if (start != null && end != null && filtered.isEmpty()) {
            if (brand != null) {
                return "Nu am găsit niciun " + brand.toUpperCase(Locale.ROOT) +
                        " disponibil în intervalul [" + start + " → " + end + "].";
            }
            return "Nu am găsit nicio mașină disponibilă în intervalul [" + start + " → " + end + "] pentru criteriile date.";
        }

        if (filtered.isEmpty()) {
            return "Nu am găsit potriviri pentru criteriile tale. Poți ajusta brandul, transmisia, combustibilul sau bugetul.";
        }

        StringBuilder sb = new StringBuilder();
        if (start != null && end != null) {
            sb.append("Opțiuni disponibile în intervalul [").append(start).append(" → ").append(end).append("]:\n");
        } else {
            sb.append("Potriviri găsite:\n");
        }
        int limit = Math.min(3, filtered.size());
        for (int i = 0; i < limit; i++) {
            sb.append(formatCarLine(filtered.get(i)));
            if (i < limit - 1) sb.append("\n");
        }
        if (filtered.size() > limit) {
            sb.append("\n…și alte ").append(filtered.size() - limit).append(" potriviri.");
        }
        return sb.toString();
    }

    /* ====================== HELPERI ====================== */

    // Găsește o mașină după mențiuni în text (brand/model), scorând simplu
    private Car findCarByMention(String message) {
        if (message == null) return null;
        String t = message.toLowerCase(Locale.ROOT);

        List<Car> all = carRepository.findAll();
        Car best = null;
        int bestScore = -1;

        for (Car c : all) {
            String brand = safe(c.getBrand()).toLowerCase(Locale.ROOT);
            String model = safe(c.getModel()).toLowerCase(Locale.ROOT);

            int score = 0;
            if (!brand.isBlank() && t.contains(brand)) score += 1;
            if (!model.isBlank() && t.contains(model)) score += 2; // modelul cântărește mai mult
            if (score > bestScore) { bestScore = score; best = c; }
        }

        return bestScore >= 1 ? best : null;
    }

    private static class DateRange {
        final LocalDateTime start;
        final LocalDateTime end;
        DateRange(LocalDateTime s, LocalDateTime e){ this.start=s; this.end=e; }
    }
    private DateRange parseDateRange(String text) {
        var dates = extractDates(text);
        if (dates.size() < 2) return null;
        var start = normalize(dates.get(0), true);
        var end   = normalize(dates.get(1), false);
        return new DateRange(start, end);
    }

    private String formatQuote(Car car, LocalDateTime start, LocalDateTime end) {
        long days = Math.max(1, java.time.Duration.between(start, end).toDays());
        int price = car.getPrice_per_day() == null ? 0 : car.getPrice_per_day();
        double subtotal = days * price;
        double deposit  = Math.round(subtotal * 0.20 * 100.0) / 100.0;
        double total    = Math.round((subtotal + deposit) * 100.0) / 100.0;

        String base = String.format(Locale.ROOT,
                "%s %s (%d) — %s, %s — %d RON/zi",
                safe(car.getBrand()), safe(car.getModel()),
                car.getYear() == null ? 0 : car.getYear(),
                safe(car.getFuel_type()), safe(car.getTransmission()), price
        );

        return base + String.format(Locale.ROOT,
                "\nInterval: [%s → %s] • Zile: %d\nSubtotal: %.2f RON\nDepozit (20%%): %.2f RON\nTotal: %.2f RON",
                start, end, days, subtotal, deposit, total
        );
    }

    private boolean isGeneralListQuestion(String msg) {
        if (msg == null) return false;
        String t = msg.toLowerCase(Locale.ROOT).trim();
        return t.contains("ce masini aveti")
                || t.contains("ce mașini aveți")
                || t.contains("ce masini aveti disponibile")
                || t.contains("ce mașini aveți disponibile")
                || t.contains("toate masinile")
                || t.contains("toate mașinile");
    }

    private static String formatCarLine(Car c) {
        return String.format(Locale.ROOT,
                "• %s %s (%d) • %s, %s • %s RON/zi",
                safe(c.getBrand()),
                safe(c.getModel()),
                c.getYear() == null ? 0 : c.getYear(),
                safe(c.getFuel_type()),
                safe(c.getTransmission()),
                c.getPrice_per_day() == null ? 0 : c.getPrice_per_day()
        );
    }

    private static Car textMode(Car c) { return c; }

    private static String safe(Object o) {
        return o == null ? "-" : String.valueOf(o);
    }

    private static boolean containsIgnoreCase(String hay, String needle) {
        if (hay == null || needle == null) return false;
        return hay.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }

    private static Long extractId(String text) {
        Matcher m = Pattern.compile("id\\s*=\\s*(\\d+)", Pattern.CASE_INSENSITIVE).matcher(text);
        if (m.find()) {
            try { return Long.parseLong(m.group(1)); } catch (Exception ignored) {}
        }
        return null;
    }

    private static List<String> extractDates(String text) {
        List<String> found = new ArrayList<>();
        Matcher d = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}(?:[ T]\\d{2}:\\d{2}(?::\\d{2})?)?)").matcher(text);
        while (d.find()) {
            found.add(d.group(1));
        }
        return found;
    }

    private static LocalDateTime normalize(String raw, boolean isStart) {
        for (DateTimeFormatter f : DATE_FORMATS) {
            try {
                if (f.toString().contains("yyyy-MM-dd") && !raw.contains(":")) {
                    LocalDateTime d = LocalDateTime.parse(raw + "T" + (isStart ? "00:00" : "23:59"), DATE_FORMATS[1]);
                    return d;
                }
                return LocalDateTime.parse(raw.replace(' ', 'T'), f);
            } catch (Exception ignored) {}
        }
        try {
            if (raw.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDateTime.parse(raw + "T" + (isStart ? "00:00" : "23:59"), DATE_FORMATS[1]);
            }
        } catch (Exception ignored) {}
        throw new IllegalArgumentException("Dată invalidă: " + raw);
    }

    private static boolean mightBeCriteriaSearch(String text) {
        if (text == null) return false;
        String t = text.toLowerCase(Locale.ROOT);

        boolean mentionsRonPrice =
                t.matches(".*\\b\\d+\\s*(?:ron|lei|leu)\\b.*") ||
                        t.matches(".*\\b\\d+\\s*(?:ron|lei|leu)\\s*(?:/|pe)?\\s*zi\\b.*") ||
                        t.matches(".*\\bla\\s*\\d+\\b.*"); // "la 100"

        return t.contains("automat") || t.contains("manual")
                || t.contains("benz") || t.contains("diesel")
                || t.contains("sub ") || t.contains("maxim ")
                || t.contains("între ") || t.contains("intre ")
                || containsAnyBrand(t)
                || mentionsRonPrice;
    }

    private static boolean containsAnyBrand(String t) {
        String[] common = {"bmw","audi","mercedes","vw","volkswagen","skoda","toyota","honda","ford","dacia","opel","renault","hyundai","kia","volvo","ferrari","porsche"};
        for (String b : common) if (t.contains(b)) return true;
        return false;
    }

    private static String extractBrand(String text) {
        String t = text.toLowerCase(Locale.ROOT);
        String[] common = {"bmw","audi","mercedes","vw","volkswagen","skoda","toyota","honda","ford","dacia","opel","renault","hyundai","kia","volvo","ferrari","porsche"};
        for (String b : common) if (t.contains(b)) return b.equals("vw")? "volkswagen" : b;
        return null;
    }

    private static String extractTransmission(String text) {
        String t = text.toLowerCase(Locale.ROOT);
        if (t.contains("automat")) return "automat";
        if (t.contains("manual"))  return "manual";
        return null;
    }

    private static String extractFuel(String text) {
        String t = text.toLowerCase(Locale.ROOT);
        if (t.contains("benz"))  return "benzin";
        if (t.contains("diesel")) return "diesel";
        return null;
    }

    // BUGFIX: ignoră anii din date; ia doar numere cu context de preț
    private static Integer extractBudget(String text) {
        if (text == null) return null;
        String t = text.toLowerCase(Locale.ROOT);
        String tNoDates = t.replaceAll("\\d{4}-\\d{2}-\\d{2}(?:[ t]\\d{2}:\\d{2}(?::\\d{2})?)?", " ");

        Matcher m = Pattern.compile("(?:sub|maxim|p(?:a|ă)na la|p\\u0103n\\u0103 la|budget|buget)\\s*(\\d{1,5})").matcher(tNoDates);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (Exception ignored) {}
        }

        m = Pattern.compile("\\b(\\d{1,5})\\s*(?:ron|lei|leu)\\b").matcher(tNoDates);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (Exception ignored) {}
        }
        return null;
    }

    // BUGFIX: ignoră anii din date; acceptă DOAR numere cu context clar de preț
    private static Integer extractExactPrice(String text) {
        if (text == null) return null;
        String t = text.toLowerCase(Locale.ROOT);
        String tNoDates = t.replaceAll("\\d{4}-\\d{2}-\\d{2}(?:[ t]\\d{2}:\\d{2}(?::\\d{2})?)?", " ");

        Pattern[] patterns = new Pattern[] {
                Pattern.compile("\\bla\\s*(\\d{1,5})\\s*(?:ron|lei|leu)?\\b"),
                Pattern.compile("\\b(\\d{1,5})\\s*(?:ron|lei|leu)\\s*(?:/|pe)?\\s*zi\\b"),
                Pattern.compile("\\b(\\d{1,5})\\s*(?:ron|lei|leu)\\b")
        };

        for (Pattern p : patterns) {
            Matcher m = p.matcher(tNoDates);
            if (m.find()) {
                try { return Integer.parseInt(m.group(1)); } catch (Exception ignored) {}
            }
        }
        return null;
    }
}
