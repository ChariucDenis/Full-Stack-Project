import React, { useEffect, useState, useRef } from 'react';
import { useParams } from 'react-router-dom';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import 'leaflet-routing-machine/dist/leaflet-routing-machine.css';
import 'leaflet-routing-machine';
import emailjs from 'emailjs-com';

const ViewCarImageComponent = () => {
    const { id } = useParams();
    const [carImage, setCarImage] = useState(null);
    const [carDetails, setCarDetails] = useState(null);
    const [location, setLocation] = useState({ lat: 47.6519, lng: 26.2555 }); // Coordonatele pentru Suceava
    const [userLocation, setUserLocation] = useState(null); // Locația personalizată introdusă de utilizator
    const [cost, setCost] = useState(null); // Costul total pentru distanța parcursă
    const [error, setError] = useState(''); // Mesajul de eroare
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [email, setEmail] = useState('');
    const [phone, setPhone] = useState('');
    const [address, setAddress] = useState('');
    const [judet, setJudet] = useState('');
    const [orasComuna, setOrasComuna] = useState('');

    const mapRef = useRef(null); // Referință pentru hartă
    const routingControlRef = useRef(null); // Referință pentru controlul de rutare

    useEffect(() => {
        // Fetch car details
        fetch(`http://localhost:8080/api/v1/car/${id}`)
            .then(res => {
                if (!res.ok) {
                    throw new Error(`HTTP error! Status: ${res.status}`);
                }
                return res.json();
            })
            .then(data => {
                setCarDetails(data);
                if (data.location) {
                    setLocation({ lat: data.location.lat, lng: data.location.lng });
                }
            })
            .catch(error => {
                console.error(`Error fetching car details for car ${id}:`, error);
                setError("Failed to load car details.");
            });

        // Fetch car image
        fetch(`http://localhost:8080/api/v1/car/${id}/image`)
            .then(res => {
                if (!res.ok) {
                    throw new Error(`HTTP error! Status: ${res.status}`);
                }
                return res.blob();
            })
            .then(blob => {
                const imageUrl = URL.createObjectURL(blob);
                setCarImage(imageUrl);
            })
            .catch(error => {
                console.error(`Error fetching image data for car ${id}:`, error);
                setError("Failed to load image.");
            });
    }, [id]);

    useEffect(() => {
        // Inițializează harta doar o singură dată
        if (!mapRef.current) {
            mapRef.current = L.map('map').setView(location, 13);

            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            }).addTo(mapRef.current);

            L.marker([location.lat, location.lng]).addTo(mapRef.current)
                .bindPopup("Car Location")
                .openPopup();
        }
    }, [location]);

    const calculateCostAndRoute = () => {
        if (!judet.trim() || !orasComuna.trim()) {
            setError('Vă rugăm să completați județul și orașul/comuna.');
            return;
        }
        // Construiți adresa completă din câmpurile introduse
        const address = `${orasComuna}, ${judet}, Romania`;

        // Folosiți adresa pentru a obține coordonatele locației
        fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}`)
            .then(response => response.json())
            .then(data => {
                if (data && data.length > 0) {
                    const { lat, lon } = data[0];
                    const userLoc = { lat: parseFloat(lat), lng: parseFloat(lon) };
                    setUserLocation(userLoc);
                    setError(''); // Resetează mesajul de eroare

                    if (mapRef.current) {
                        // Elimină ruta anterioară dacă există una
                        if (routingControlRef.current) {
                            mapRef.current.removeControl(routingControlRef.current);
                        }

                        // Adaugă noul traseu pe hartă
                        routingControlRef.current = L.Routing.control({
                            waypoints: [
                                L.latLng(location.lat, location.lng),
                                L.latLng(userLoc.lat, userLoc.lng)
                            ],
                            lineOptions: {
                                styles: [{ color: '#6FA1EC', weight: 4 }]
                            },
                            createMarker: function() { return null; }, // Elimină markerii din traseu
                            addWaypoints: false,
                            draggableWaypoints: false,
                            fitSelectedRoutes: true,
                            showAlternatives: false
                        })
                        .on('routesfound', function(e) {
                            const routes = e.routes;
                            const summary = routes[0].summary;
                            const totalDistanceKm = summary.totalDistance / 1000; // Convertiți distanța în km
                            
                            if (carDetails) {
                                let fuelPricePerLiter;
                                
                                // Setează prețul pe litru în funcție de tipul de combustibil
                                switch (carDetails.fuel_type) {
                                    case 'Motorină':
                                        fuelPricePerLiter = 8;
                                        break;
                                    case 'Benzină':
                                    case 'Hibrid':
                                        fuelPricePerLiter = 7;
                                        break;
                                    case 'Electrică':
                                        setCost(0); // Setează costul la 0 pentru vehicule electrice
                                        return; // Nu mai efectua alte calcule
                                    default:
                                        setError('Tipul de combustibil nu este specificat corect.');
                                        return;
                                }

                                // Calculați totalul de combustibil consumat
                                const totalFuelConsumed = (totalDistanceKm / 100) * carDetails.fuel_consumption;
                                const totalCost = (totalFuelConsumed * fuelPricePerLiter*3).toFixed(2); // Calculați costul total în lei
                                setCost(totalCost); // Setează costul în lei
                            } else {
                                setError("Detaliile mașinii nu sunt încărcate corespunzător.");
                            }
                        })
                        .addTo(mapRef.current);
                    }
                } else {
                    setError('Locatia nu a fost gasita');
                }
            })
            .catch(err => {
                console.error('Error fetching user location:', err);
                setError('Failed to fetch the location. Please try again.');
            });
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        const rezervareData = {
            carId: id,
            firstName: firstName,
            lastName: lastName,
            email: email,
            phone: phone,
            address: address,
        };

        // Send the reservation data to the backend to save it in the database
        fetch('http://localhost:8080/api/v1/rezervari', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(rezervareData),
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to save rezervare');
            }
            return response.json();
        })
        .then(data => {
            console.log('Rezervare saved successfully:', data);
            
            // Now send the email using EmailJS
            const templateParams = {
                from_name: `${firstName} ${lastName}`,
                to_name: 'Denis Chariuc',
                message: `Nume: ${firstName} ${lastName}\nEmail: ${email}\nPhone: ${phone}\nAddress: ${address}\nID-ul Mașinii: ${id}`,
                to_email: 'denis.chariuc@gmail.com'
            };

            return emailjs.send(
                'service_2i7piwf',         // ID-ul serviciului din EmailJS
                'template_nftsfsa',        // ID-ul șablonului din EmailJS
                templateParams,
                '8U92NtaqpcQ4vXQhs'        // ID-ul utilizatorului din EmailJS
            );
        })
        .then(response => {
            console.log('Email successfully sent!', response.status, response.text);
            
            // Reset the form fields after successful submission
            setFirstName('');
            setLastName('');
            setEmail('');
            setPhone('');
            setAddress('');
        })
        .catch(error => {
            console.error('Error processing request:', error);
        });
    };

    return (
        <div className='container-large'>
            <h2 className='text-center'>
                {carDetails ? `${carDetails.brand} ${carDetails.model}` : 'Car Details'}
            </h2>
            {carDetails && (
                <div className='car-details'>
                    {carImage && (
                        <img 
                            src={carImage} 
                            alt={`Car ${carDetails.brand} ${carDetails.model}`} 
                            className="car-image-large"
                        />
                    )}
                    <div className='car-info'>
                        <p><strong>Brand:</strong> {carDetails.brand}</p>
                        <p><strong>Model:</strong> {carDetails.model}</p>
                        <p><strong>An:</strong> {carDetails.year}</p>
                        <p><strong>Culoare:</strong> {carDetails.color}</p>
                        <p><strong>Cutie de viteze:</strong> {carDetails.transmission}</p>
                        <p><strong>Combustibil:</strong> {carDetails.fuel_type}</p>
                        <p><strong>Pret pe zi:</strong> {carDetails.price_per_day} RON</p>
                        <p><strong>Consum:</strong> {carDetails.fuel_consumption} l/100 km</p>
                        <p><strong>CO2:</strong> {carDetails.emissions} g/km</p>
                    </div>
                </div>
            )}
            {/* Container separat pentru hartă */}
            <div className='map-container'>
                <h4 className='text-center'>Locația mașinii</h4>
                <div id='map' style={{ height: "500px", width: "100%" }}></div>
                <div className='location-input'>
                   <p>Adaugă județul și orașul unde dorești sa îți livrăm mașina</p>
                    <input 
                        type='text' 
                        placeholder='Județ' 
                        value={judet}
                        onChange={(e) => setJudet(e.target.value)} 
                    />
                    <input 
                        type='text' 
                        placeholder='Oraș/Comuna' 
                        value={orasComuna}
                        onChange={(e) => setOrasComuna(e.target.value)} 
                    />
                    <button className="custom-button-rute" onClick={calculateCostAndRoute}>Afișează ruta și prețul</button>
                    {cost !== null && (
                        <p><strong>Cost:</strong> {cost} lei</p>
                    )}
                    {error && (
                        <p style={{ color: 'red' }}>{error}</p>
                    )}
                </div>
            </div>
            {/* Formular pentru trimiterea numelui, prenumelui, email, număr de telefon, adresă și ID-ului mașinii */}
            <div className='email-form'>
                <h4 className='text-center'>Adaugați datele de contact</h4>
                <form onSubmit={handleSubmit} className="custom-form">
                    <div className="form-group">
                       
                        <label htmlFor="lastName">Nume:</label>
                        <input
                            type="text"
                            id="lastName"
                            value={lastName}
                            onChange={(e) => setLastName(e.target.value)}
                            required
                        />
                    </div>
                    <div className="form-group">
                    <label htmlFor="firstName">Prenume:</label>
                        <input
                            type="text"
                            id="firstName"
                            value={firstName}
                            onChange={(e) => setFirstName(e.target.value)}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="email">Email:</label>
                        <input
                            type="email"
                            id="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="phone">Telefon:</label>
                        <input
                            type="tel"
                            id="phone"
                            value={phone}
                            onChange={(e) => setPhone(e.target.value)}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="address">Adresă:</label>
                        <input
                            type="text"
                            id="address"
                            value={address}
                            onChange={(e) => setAddress(e.target.value)}
                            required
                        />
                    </div>
                    <button type="submit" className="submit-button">Rezervă mașina</button>
                </form>
            </div>
        </div>
    );
};

export default ViewCarImageComponent;
