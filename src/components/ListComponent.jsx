import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { deleteCar, listCars } from '../services/employeeService';

const ListComponent = () => {
    const [cars, setCars] = useState([]);
    const [currentPage, setCurrentPage] = useState(1);
    const [carsPerPage] = useState(10);
    const navigate = useNavigate();

    useEffect(() => {
        listCars().then((response) => {
            const carData = response.data;

            carData.forEach(car => {
                fetch(`http://localhost:8080/api/v1/car/${car.id}/image`)
                    .then(res => {
                        if (!res.ok) {
                            throw new Error(`HTTP error! Status: ${res.status}`);
                        }
                        return res.blob();
                    })
                    .then(blob => {
                        if (blob.size === 0) {
                            throw new Error('Blob is empty!');
                        }
                        const imageUrl = URL.createObjectURL(blob);
                        setCars(prevCars => 
                            prevCars.map(c => 
                                c.id === car.id ? { ...c, imageUrl } : c
                            )
                        );
                    })
                    .catch(error => {
                        console.error(`Error fetching image for car ${car.id}:`, error);
                    });
            });

            setCars(carData);
        }).catch(error => {
            console.error("Error fetching car data:", error);
        });
    }, []);

    const addNewCar = () => {
        navigate('/add-car');
    };

    const updateCar = (id) => {
        navigate(`/edit-car/${id}`);
    };

    const removeCar = (id) => {
        deleteCar(id).then(() => {
            setCars(cars.filter(car => car.id !== id));
        }).catch(error => {
            console.error("Error deleting car:", error);
        });
    };

    const indexOfLastCar = currentPage * carsPerPage;
    const indexOfFirstCar = indexOfLastCar - carsPerPage;
    const currentCars = cars.slice(indexOfFirstCar, indexOfLastCar);
    const totalPages = Math.ceil(cars.length / carsPerPage);

    const paginate = (pageNumber) => setCurrentPage(pageNumber);

    return (
        <div className='container'>
            <h2 className='text-center'>Lista de mașini</h2>
            <button className="btn btn-primary mb-3" onClick={addNewCar}>Adăugare</button>
            <table className='table table-striped table-bordered'>
                <thead>
                    <tr>
                        <th>Id mașină</th>
                        <th>Imagine</th>
                        <th>Brand</th>
                        <th>Model</th>
                        <th>An</th>
                        <th>Culoare</th>
                        <th>Cutie de viteze</th>
                        <th>Combustibil</th>
                        <th>Preț</th>
                        <th>Emisii CO2</th>
                        <th>Consum combustibil</th>
                        <th>Acțiuni</th>
                    </tr>
                </thead>
                <tbody>
                    {currentCars.map(car => (
                        <tr key={car.id}>
                            <td>{car.id}</td>
                            <td>
                                {car.imageUrl ? (
                                    <img 
                                        src={car.imageUrl} 
                                        alt={`${car.brand} ${car.model}`} 
                                        style={{ width: '100px', height: 'auto' }} 
                                    />
                                ) : (
                                    <span>Loading Image...</span>
                                )}
                            </td>
                            <td>{car.brand}</td>
                            <td>{car.model}</td>
                            <td>{car.year}</td>
                            <td>{car.color}</td>
                            <td>{car.transmission}</td>
                            <td>{car.fuel_type}</td>
                            <td>{car.price_per_day}</td>
                            <td>{car.co2_emissions}</td> {/* Accesează co2_emissions */}
                            <td>{car.fuel_consumption}</td>
                            <td>
                                <button className='btn btn-info' onClick={() => updateCar(car.id)}>Modificare</button>
                                <button className='btn btn-danger' onClick={() => removeCar(car.id)}>Ștergere</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

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

export default ListComponent;
