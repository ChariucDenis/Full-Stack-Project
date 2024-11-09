import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getCar, createCar, updateCar } from '../services/employeeService';

const CarComponent = () => {
    const [brand, setBrand] = useState('');
    const [model, setModel] = useState('');
    const [year, setYear] = useState('');
    const [color, setColor] = useState('');
    const [transmission, setTransmission] = useState('');
    const [fuel_type, setFuel_Type] = useState('');
    const [price_per_day, setPrice_Per_Day] = useState('');
    const [emissions, setEmissions] = useState(''); // State for emissions
    const [fuel_consumption, setFuelConsumption] = useState(''); // State for fuel consumption
    const [image, setImage] = useState(null);
    const [imagePreview, setImagePreview] = useState(null);

    const { id } = useParams();
    const [errors, setErrors] = useState({
        brand: '',
        model: '',
        year: '',
        color: '',
        transmission: '',
        fuel_type: '',
        price_per_day: '',
        emissions: '', // Error for emissions
        fuel_consumption: '', // Error for fuel consumption
    });

    const navigate = useNavigate();

    const fuelTypes = ["Benzină", "Motorină", "Electrică", "Hibrid"];
    const transmissions = ["Automată", "Manuală", "Semi-automată"];
    const years = Array.from({ length: 2024 - 1900 + 1 }, (_, index) => 2024 - index);

    useEffect(() => {
        if (id) {
            getCar(id).then((response) => {
                setBrand(response.data.brand);
                setModel(response.data.model);
                setYear(response.data.year);
                setColor(response.data.color);
                setTransmission(response.data.transmission);
                setFuel_Type(response.data.fuel_type);
                setPrice_Per_Day(response.data.price_per_day);
                setEmissions(response.data.emissions);
                setFuelConsumption(response.data.fuel_consumption);

                if (response.data.image) {
                    const imageUrl = `data:${response.data.imageType};base64,${response.data.image}`;
                    setImagePreview(imageUrl); 
                }
            }).catch(error => {
                console.error(error);
            });
        }
    }, [id]);

    function handleBrand(e) {
        setBrand(e.target.value);
    }

    function handleModel(e) {
        setModel(e.target.value);
    }

    function handleYear(e) {
        setYear(e.target.value);
    }

    function handleColor(e) {
        setColor(e.target.value);
    }

    function handleTransmission(e) {
        setTransmission(e.target.value);
    }

    function handleFuel_Type(e) {
        setFuel_Type(e.target.value);
    }

    function handlePrice_Per_Day(e) {
        setPrice_Per_Day(e.target.value);
    }

    function handleEmissions(e) {
        setEmissions(e.target.value);
    }

    function handleFuelConsumption(e) {
        setFuelConsumption(e.target.value);
    }

    function handleImageChange(e) {
        const file = e.target.files[0];
        setImage(file);
        setImagePreview(URL.createObjectURL(file));
    }

    function saveOrUpdateCar(e) {
        e.preventDefault();

        const formData = new FormData();
        formData.append('brand', brand);
        formData.append('model', model);
        formData.append('year', year);
        formData.append('color', color);
        formData.append('transmission', transmission);
        formData.append('fuel_type', fuel_type);
        formData.append('price_per_day', price_per_day);
        formData.append('emissions', emissions);
        formData.append('fuel_consumption', fuel_consumption);
        if (image) {
            formData.append('image', image);
        }

        if (id) {
            updateCar(id, formData).then((response) => {
                console.log(response.data);
                navigate('/car');
            }).catch(error => {
                console.error(error);
            });
        } else {
            if (validateForm()) {
                createCar(formData).then((response) => {
                    console.log(response.data);
                    navigate('/car');
                }).catch(error => {
                    console.error(error);
                });
            }
        }
    }

    function validateForm() {
        let valid = true;
        const errorsCopy = { ...errors };

        if (brand.trim()) {
            errorsCopy.brand = '';
        } else {
            errorsCopy.brand = "Brandul este obligatoriu";
            valid = false;
        }

        if (model.trim()) {
            errorsCopy.model = '';
        } else {
            errorsCopy.model = "Modelul este obligatoriu";
            valid = false;
        }

        if (year.trim()) {
            errorsCopy.year = '';
        } else {
            errorsCopy.year = "Anul este obligatoriu";
            valid = false;
        }

        if (color.trim()) {
            errorsCopy.color = '';
        } else {
            errorsCopy.color = "Culoarea este obligatorie";
            valid = false;
        }

        if (transmission.trim()) {
            errorsCopy.transmission = '';
        } else {
            errorsCopy.transmission = "Cutia de viteze este obligatorie";
            valid = false;
        }

        if (fuel_type.trim()) {
            errorsCopy.fuel_type = '';
        } else {
            errorsCopy.fuel_type = "Tipul de combustibil este obligatoriu";
            valid = false;
        }

        if (price_per_day.trim()) {
            errorsCopy.price_per_day = '';
        } else {
            errorsCopy.price_per_day = "Prețul pe zi este obligatoriu";
            valid = false;
        }

        if (emissions.trim()) {
            errorsCopy.emissions = '';
        } else {
            errorsCopy.emissions = "Emisiile sunt obligatorii";
            valid = false;
        }

        if (fuel_consumption.trim()) {
            errorsCopy.fuel_consumption = '';
        } else {
            errorsCopy.fuel_consumption = "Consum de combustibil este obligatoriu";
            valid = false;
        }

        setErrors(errorsCopy);
        return valid;
    }

    function pageTitle() {
        if (id) {
            return <h2 className='text-center'>Modifică mașina</h2>;
        } else {
            return <h2 className='text-center'>Adaugă mașina</h2>;
        }
    }

    return (
        <div className='container'>
            <br /> <br />
            <div className='row'>
                <div className='card col-md-6 offset-md-3 offset-md-3'>
                    {pageTitle()}
                    <div className='card-body'>
                        <form>
                            <div className='form-group mb-2'>
                                <label className='form-label'>Brand:</label>
                                <input
                                    type='text'
                                    placeholder='Introduceți brandul mașinii'
                                    name='brand'
                                    value={brand}
                                    className={`form-control ${errors.brand ? 'is-invalid' : ''}`}
                                    onChange={handleBrand}
                                />
                                {errors.brand && <div className='invalid-feedback'>{errors.brand}</div>}
                            </div>
                            <div className='form-group mb-2'>
                                <label className='form-label'>Model:</label>
                                <input
                                    type='text'
                                    placeholder='Introduceți modelul mașinii'
                                    name='model'
                                    value={model}
                                    className={`form-control ${errors.model ? 'is-invalid' : ''}`}
                                    onChange={handleModel}
                                />
                                {errors.model && <div className='invalid-feedback'>{errors.model}</div>}
                            </div>
                            <div className='form-group mb-2'>
                                <label className='form-label'>An:</label>
                                <select
                                    name='year'
                                    value={year}
                                    className={`form-control ${errors.year ? 'is-invalid' : ''}`}
                                    onChange={handleYear}
                                >
                                    <option value=''>Selectați anul</option>
                                    {years.map(y => (
                                        <option key={y} value={y}>{y}</option>
                                    ))}
                                </select>
                                {errors.year && <div className='invalid-feedback'>{errors.year}</div>}
                            </div>
                            <div className='form-group mb-2'>
                                <label className='form-label'>Culoare:</label>
                                <input
                                    type='text'
                                    placeholder='Introduceți culoarea mașinii'
                                    name='color'
                                    value={color}
                                    className={`form-control ${errors.color ? 'is-invalid' : ''}`}
                                    onChange={handleColor}
                                />
                                {errors.color && <div className='invalid-feedback'>{errors.color}</div>}
                            </div>
                            <div className='form-group mb-2'>
                                <label className='form-label'>Cutie de viteze:</label>
                                <select
                                    name='transmission'
                                    value={transmission}
                                    className={`form-control ${errors.transmission ? 'is-invalid' : ''}`}
                                    onChange={handleTransmission}
                                >
                                    <option value=''>Selectați cutia de viteze</option>
                                    {transmissions.map(t => (
                                        <option key={t} value={t}>{t}</option>
                                    ))}
                                </select>
                                {errors.transmission && <div className='invalid-feedback'>{errors.transmission}</div>}
                            </div>
                            <div className='form-group mb-2'>
                                <label className='form-label'>Tip combustibil:</label>
                                <select
                                    name='fuel_type'
                                    value={fuel_type}
                                    className={`form-control ${errors.fuel_type ? 'is-invalid' : ''}`}
                                    onChange={handleFuel_Type}
                                >
                                    <option value=''>Selectați tipul de combustibil</option>
                                    {fuelTypes.map(f => (
                                        <option key={f} value={f}>{f}</option>
                                    ))}
                                </select>
                                {errors.fuel_type && <div className='invalid-feedback'>{errors.fuel_type}</div>}
                            </div>
                            <div className='form-group mb-2'>
                                <label className='form-label'>Preț pe zi:</label>
                                <input
                                    type='text'
                                    placeholder='Introduceți prețul pe zi'
                                    name='price_per_day'
                                    value={price_per_day}
                                    className={`form-control ${errors.price_per_day ? 'is-invalid' : ''}`}
                                    onChange={handlePrice_Per_Day}
                                />
                                {errors.price_per_day && <div className='invalid-feedback'>{errors.price_per_day}</div>}
                            </div>
                            <div className='form-group mb-2'>
                                <label className='form-label'>Emisii:</label>
                                <input
                                    type='text'
                                    placeholder='Introduceți emisiile'
                                    name='emissions'
                                    value={emissions}
                                    className={`form-control ${errors.emissions ? 'is-invalid' : ''}`}
                                    onChange={handleEmissions}
                                />
                                {errors.emissions && <div className='invalid-feedback'>{errors.emissions}</div>}
                            </div>
                            <div className='form-group mb-2'>
                                <label className='form-label'>Consum combustibil:</label>
                                <input
                                    type='text'
                                    placeholder='Introduceți consumul de combustibil'
                                    name='fuel_consumption'
                                    value={fuel_consumption}
                                    className={`form-control ${errors.fuel_consumption ? 'is-invalid' : ''}`}
                                    onChange={handleFuelConsumption}
                                />
                                {errors.fuel_consumption && <div className='invalid-feedback'>{errors.fuel_consumption}</div>}
                            </div>
                            <div className='form-group mb-2'>
                                <label className='form-label'>Imagine:</label>
                                <input
                                    type='file'
                                    name='image'
                                    className='form-control'
                                    onChange={handleImageChange}
                                />
                            </div>
                            {imagePreview && <img src={imagePreview} alt="Car" style={{ marginTop: '20px', maxWidth: '100%' }} />}
                            <button className='btn btn-success' onClick={saveOrUpdateCar}>Trimite</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CarComponent;
