import React, { useEffect, useState } from 'react';
import car1 from './images/img1.png';
import car2 from './images/img2.jpg';
import car3 from './images/img3.jpg';
import { useNavigate } from 'react-router-dom';

const Home = () => {
    const images = [car1, car2, car3];
    const [currentImageIndex, setCurrentImageIndex] = useState(0);
    const [cars, setCars] = useState([]);
    const [showPrice, setShowPrice] = useState({}); // Obiect pentru a gestiona ce preturi sunt afișate

    const navigate = useNavigate();

    useEffect(() => {
        // Schimbare automată a imaginilor la fiecare 3 secunde
        const interval = setInterval(() => {
            setCurrentImageIndex((prevIndex) => (prevIndex + 1) % images.length);
        }, 10000);

        return () => clearInterval(interval);
    }, [images.length]);

    useEffect(() => {
        // Fetch pentru a obține datele mașinilor
        fetch('http://localhost:8080/api/v1/car')
            .then(response => response.json())
            .then(data => setCars(data))
            .catch(error => console.error('Error fetching cars:', error));
    }, []);

    const viewCarDetails = (id) => {
        navigate(`/view-car-image/${id}`);
        window.scrollTo(0, 0);
    };

    const prevSlide = () => {
        setCurrentImageIndex((prevIndex) => (prevIndex - 1 + images.length) % images.length);
    };

    const nextSlide = () => {
        setCurrentImageIndex((prevIndex) => (prevIndex + 1) % images.length);
    };

    const handleShowPrice = (id) => {
        setShowPrice((prevState) => ({ ...prevState, [id]: !prevState[id] }));
    };

    return (
        <div className="custom-home-container">
            <div className="custom-slider">
                <img 
                    src={images[currentImageIndex]} 
                    alt="slider" 
                    className="custom-slider-image" 
                />
                <button className="custom-prev-button" onClick={prevSlide}>&#10094;</button>
                <button className="custom-next-button" onClick={nextSlide}>&#10095;</button>
            </div>
            <h2 className='text-center'>Mașini disponibile pentru închirieri</h2>
            <div className='custom-cars-grid'>
                {cars.slice(0, 3).map(car => (
                    <div className='custom-car-card' key={car.id}>
                        <img 
                            src={`http://localhost:8080/api/v1/car/${car.id}/image`} 
                            alt={`${car.brand} ${car.model}`} 
                            className='custom-car-image'
                            onClick={() => viewCarDetails(car.id)} // Adăugăm evenimentul onClick aici
                            style={{ cursor: 'pointer' }} // Schimbăm cursorul la pointer
                        />
                        <button 
                            className='btn btn-primary custom-reserve-btn' 
                            onClick={() => viewCarDetails(car.id)}
                        >
                            REZERVA ACUM
                        </button>
                        <h4>{car.brand} {car.model} Edition</h4>
                        {showPrice[car.id] ? (
                            <p>{car.price_per_day} RON/zi</p>
                        ) : (
                            <button 
                                className='btn btn-secondary custom-price-btn' 
                                onClick={() => handleShowPrice(car.id)}
                            >
                                Vezi pret
                            </button>
                        )}
                        <div className='custom-car-details'>
                            <span><i className="fas fa-calendar-alt"></i> {car.year}</span>
                            <span><i className="fas fa-tachometer-alt"></i> {car.transmission}</span>
                            <span><i className="fas fa-gas-pump"></i> {car.fuel_type}</span>
                        </div>
                    </div>
                ))}
            </div>
            <div className='custom-view-all-container'>
                <button className='btn btn-warning custom-view-all-btn' onClick={() => navigate('/cars')}>
                    VEZI TOATE MASINILE
                </button>
            </div>
        </div>
    );
};

export default Home;
