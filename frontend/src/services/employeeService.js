import axios from "axios";

const REST_API_BASE_URL = 'http://localhost:8080/api/v1/car';

export const listCars = () => axios.get(REST_API_BASE_URL);
 
export const createCar = (car) => axios.post(REST_API_BASE_URL, car);

export const getCar = (carId) => axios.get(REST_API_BASE_URL + '/' + carId);

export const deleteCar = (carId) => axios.delete(REST_API_BASE_URL + '/' + carId);

export const updateCar = (carId, car) => axios.put(REST_API_BASE_URL + '/' + carId, car);
