import React, { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import 'leaflet-routing-machine/dist/leaflet-routing-machine.css';
import 'leaflet-routing-machine';

const CarGrid = () => {
    const [cars, setCars] = useState([]);
    const [searchBrand, setSearchBrand] = useState('');
    const [searchModel, setSearchModel] = useState('');
    const [currentPage, setCurrentPage] = useState(1);
    const [carsPerPage] = useState(9); // Numărul de mașini afișate pe fiecare pagină
    const [sortCriteria, setSortCriteria] = useState('standard'); // Criteriul de sortare
    const [selectedTransmissions, setSelectedTransmissions] = useState([]); // Transmisii selectate
    const [selectedFuelTypes, setSelectedFuelTypes] = useState([]); // Tipuri de combustibil selectate
    const [showPrices, setShowPrices] = useState({}); // State pentru a ține evidența prețurilor vizibile
    const [showMap, setShowMap] = useState(false); // Control vizibilitatea hărții și a câmpurilor
    const [startJudet, setStartJudet] = useState('');
    const [startOrasComuna, setStartOrasComuna] = useState('');
    const [endJudet, setEndJudet] = useState('');
    const [endOrasComuna, setEndOrasComuna] = useState('');
    const [emissions, setEmissions] = useState({}); // Emisiile calculate pentru fiecare mașină

    const mapRef = useRef(null);
    const routingControlRef = useRef(null);

    const navigate = useNavigate();

    useEffect(() => {
        fetch('http://localhost:8080/api/v1/car')
            .then(response => response.json())
            .then(data => setCars(data))
            .catch(error => console.error('Error fetching cars:', error));
    }, []);

    const handleSortChange = (e) => {
        setSortCriteria(e.target.value);
    };

    const handleTransmissionChange = (e) => {
        const { value, checked } = e.target;
        setSelectedTransmissions(prev =>
            checked ? [...prev, value] : prev.filter(trans => trans !== value)
        );
    };

    const handleFuelTypeChange = (e) => {
        const { value, checked } = e.target;
        setSelectedFuelTypes(prev =>
            checked ? [...prev, value] : prev.filter(fuel => fuel !== value)
        );
    };

    const handleShowPrice = (carId) => {
        setShowPrices(prev => ({ ...prev, [carId]: !prev[carId] }));
    };

    const sortCars = (cars) => {
        switch (sortCriteria) {
            case 'emissions':
                return cars.sort((a, b) => (emissions[a.id] || 0) - (emissions[b.id] || 0));
            case 'price-asc':
                return cars.sort((a, b) => a.price_per_day - b.price_per_day);
            case 'price-desc':
                return cars.sort((a, b) => b.price_per_day - a.price_per_day);
            case 'year-asc':
                return cars.sort((a, b) => a.year - b.year);
            case 'year-desc':
                return cars.sort((a, b) => b.year - a.year);
            default:
                return cars; // Standard, no sorting
        }
    };

    const filterCars = (cars) => {
        return cars.filter(car => {
            const matchesTransmission = selectedTransmissions.length === 0 || selectedTransmissions.includes(car.transmission);
            const matchesFuelType = selectedFuelTypes.length === 0 || selectedFuelTypes.includes(car.fuel_type);
            return matchesTransmission && matchesFuelType;
        });
    };

    const filteredCars = sortCars(
        filterCars(
            cars.filter(car => 
                car.brand.toLowerCase().includes(searchBrand.toLowerCase()) &&
                car.model.toLowerCase().includes(searchModel.toLowerCase())
            )
        )
    );

    const indexOfLastCar = currentPage * carsPerPage;
    const indexOfFirstCar = indexOfLastCar - carsPerPage;
    const currentCars = filteredCars.slice(indexOfFirstCar, indexOfLastCar);
    const totalPages = Math.ceil(filteredCars.length / carsPerPage);

    const handleCarClick = (carId) => {
        navigate(`/view-car-image/${carId}`);
        window.scrollTo(0, 0);
    };

    const paginate = (pageNumber) => setCurrentPage(pageNumber);

    const destroyMap = () => {
        if (mapRef.current) {
            mapRef.current.off(); // Dezactivează evenimentele
            mapRef.current.remove(); // Elimină harta
            mapRef.current = null; // Resetăm referința
        }
    };

    const toggleMapVisibility = () => {
        if (showMap) {
            destroyMap();
        }
        setShowMap(!showMap);
    };

    useEffect(() => {
        if (showMap && !mapRef.current) {
            mapRef.current = L.map('map').setView([47.6519, 26.2555], 8); // Centrul inițial pe Suceava

            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            }).addTo(mapRef.current);
        }
    }, [showMap]);

    const calculateRoute = () => {
        if (!startJudet.trim() || !startOrasComuna.trim() || !endJudet.trim() || !endOrasComuna.trim()) {
            alert('Vă rugăm să completați toate câmpurile.');
            return;
        }

        const startAddress = `${startOrasComuna}, ${startJudet}, Romania`;
        const endAddress = `${endOrasComuna}, ${endJudet}, Romania`;

        Promise.all([
            fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(startAddress)}`).then(res => res.json()),
            fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(endAddress)}`).then(res => res.json())
        ]).then(([startData, endData]) => {
            if (startData.length > 0 && endData.length > 0) {
                const startLocation = { lat: parseFloat(startData[0].lat), lng: parseFloat(startData[0].lon) };
                const endLocation = { lat: parseFloat(endData[0].lat), lng: parseFloat(endData[0].lon) };

                if (mapRef.current) {
                    // Elimină ruta anterioară dacă există
                    if (routingControlRef.current) {
                        mapRef.current.removeControl(routingControlRef.current);
                    }

                    // Adaugă noua rută pe hartă
                    routingControlRef.current = L.Routing.control({
                        waypoints: [
                            L.latLng(startLocation.lat, startLocation.lng),
                            L.latLng(endLocation.lat, endLocation.lng)
                        ],
                        lineOptions: {
                            styles: [{ color: '#6FA1EC', weight: 4 }]
                        },
                        createMarker: function() { return null; }, // Elimină markerii din traseu
                        addWaypoints: false,
                        draggableWaypoints: false,
                        fitSelectedRoutes: true,
                        showAlternatives: false
                    }).on('routesfound', function(e) {
                        const routes = e.routes;
                        const summary = routes[0].summary;
                        const totalDistanceKm = summary.totalDistance / 1000; // Convertiți distanța în km

                        // Calculați emisiile pentru fiecare mașină și actualizați state-ul
                        const newEmissions = {};
                        cars.forEach(car => {
                            const carEmissionsPerKm = car.emissions; // Folosim câmpul 'emissions' din baza de date
                            newEmissions[car.id] = (carEmissionsPerKm * totalDistanceKm).toFixed(2);
                        });
                        setEmissions(newEmissions);

                        // Setează criteriul de sortare pentru a afișa mașinile în ordinea emisiilor
                        setSortCriteria('emissions');
                    }).addTo(mapRef.current);
                }
            } else {
                alert('Locațiile nu au putut fi găsite. Vă rugăm să verificați adresele introduse.');
            }
        }).catch(err => {
            console.error('Error fetching route data:', err);
            alert('A apărut o eroare la calcularea rutei.');
        });
    };

    return (
        <div>
             {/* Titlu pagină */}
             <h1 className='car-grid-title'>Mașini de închiriat</h1>

{/* Text despre importanța evitării poluării */}
<p className="environment-info">
    Este esențial să reducem emisiile de carbon în fiecare călătorie, 
    iar închirierea unei mașini eficiente sau electrice poate contribui semnificativ la acest obiectiv.
</p>  
           
            <button className="show-hide-map-btn" onClick={toggleMapVisibility}>
                {showMap ? 'Ascunde Harta și Câmpurile' : 'Calculeaza emisiile de carbon'}
            </button>

            {showMap && (
                <>
                    <div id="map" style={{ height: '300px', width: '100%', marginBottom: '15px' }}></div>
                    <div className="location-inputs">
                        
                        <div>
                            
                            <input 
                                type="text" 
                                placeholder="Județ Pornire" 
                                value={startJudet}
                                onChange={(e) => setStartJudet(e.target.value)} 
                            />
                            
                            <input 
                                type="text" 
                                placeholder="Județ Destinație" 
                                value={endJudet}
                                onChange={(e) => setEndJudet(e.target.value)} 
                            />
                        </div>
                        <div>
                        <input 
                                type="text" 
                                placeholder="Oraș/Comuna Pornire" 
                                value={startOrasComuna}
                                onChange={(e) => setStartOrasComuna(e.target.value)} 
                            />
                            <input 
                                type="text" 
                                placeholder="Oraș/Comuna Destinație" 
                                value={endOrasComuna}
                                onChange={(e) => setEndOrasComuna(e.target.value)} 
                            />
                        </div>
                        <button onClick={calculateRoute}>Calculează emisiile</button>
                    </div>
                </>
            )}

            {/* Restul componentelor */}
            <div className="search-bar">
                <input 
                    type="text" 
                    placeholder="Caută brandul dorit" 
                    value={searchBrand}
                    onChange={e => setSearchBrand(e.target.value)}
                />
                <input 
                    type="text" 
                    placeholder="Caută modelul dorit" 
                    value={searchModel}
                    onChange={e => setSearchModel(e.target.value)}
                />
                <select className="sort-dropdown" value={sortCriteria} onChange={handleSortChange}>
                    <option value="standard">Standard</option>
                    <option value="price-asc">Preț crescător</option>
                    <option value="price-desc">Preț descrescător</option>
                    <option value="year-asc">An de fabricație crescător</option>
                    <option value="year-desc">An de fabricație descrescător</option>
                    <option value="emissions">Emisii scăzute</option>
                </select>
            </div>
            <div className="filters">
                <div className="filter-group">
                    <h4>Transmisie</h4>
                    <label>
                        <input 
                            type="checkbox" 
                            value="Automată" 
                            onChange={handleTransmissionChange} 
                        />
                        Automată
                    </label>
                    <label>
                        <input 
                            type="checkbox" 
                            value="Manuală" 
                            onChange={handleTransmissionChange} 
                        />
                        Manuală
                    </label>
                    <label>
                        <input 
                            type="checkbox" 
                            value="Semi-automată" 
                            onChange={handleTransmissionChange} 
                        />
                        Semi-automată
                    </label>
                </div>
                <div className="filter-group">
                    <h4>Combustibil</h4>
                    <label>
                        <input 
                            type="checkbox" 
                            value="Benzină" 
                            onChange={handleFuelTypeChange} 
                        />
                        Benzină
                    </label>
                    <label>
                        <input 
                            type="checkbox" 
                            value="Motorină" 
                            onChange={handleFuelTypeChange} 
                        />
                        Motorină
                    </label>
                    <label>
                        <input 
                            type="checkbox" 
                            value="Hibrid" 
                            onChange={handleFuelTypeChange} 
                        />
                        Hibrid
                    </label>
                    <label>
                        <input 
                            type="checkbox" 
                            value="Electrică" 
                            onChange={handleFuelTypeChange} 
                        />
                        Electrică
                    </label>
                </div>
            </div>
            <div className="car-grid">
                {currentCars.map(car => (
                    <div className="car-card" key={car.id} onClick={() => handleCarClick(car.id)}>
                        <img 
                            src={`http://localhost:8080/api/v1/car/${car.id}/image`} 
                            alt={`${car.brand} ${car.model}`} 
                            className="car-image" 
                        />
                        <h2 className="car-title">{car.brand} {car.model}</h2>
                        {!showPrices[car.id] ? (
                            <button className="show-price-btn" onClick={(e) => {e.stopPropagation(); handleShowPrice(car.id);}}>
                                Vezi Preț
                            </button>
                        ) : (
                            <div className="car-price">
                                {car.price_per_day} RON/zi
                            </div>
                        )}
                        
                        <div className="car-info">
                            <span>{car.year}</span>
                            <span>{car.transmission}</span>
                            <span>{car.fuel_type}</span>
                        </div>
                        {emissions[car.id] && (
                            <div className="car-emissions">
                                <strong>Emisii estimate:</strong> {emissions[car.id]} g CO2
                            </div>
                        )}
                    </div>
                ))}
            </div>
            {/* Paginare */}
            <div className="pagination">
                <button 
                    className="btn btn-secondary" 
                    onClick={() => paginate(currentPage - 1)} 
                    disabled={currentPage === 1}
                >
                    Anterior
                </button>
                {[...Array(totalPages)].map((_, index) => (
                    <button 
                        key={index + 1} 
                        onClick={() => paginate(index + 1)} 
                        className={`btn ${currentPage === index + 1 ? 'btn-primary' : 'btn-light'}`}
                    >
                        {index + 1}
                    </button>
                ))}
                <button 
                    className="btn btn-secondary" 
                    onClick={() => paginate(currentPage + 1)} 
                    disabled={currentPage === totalPages}
                >
                    Următor
                </button>
            </div>
        </div>
    );
};

export default CarGrid;
