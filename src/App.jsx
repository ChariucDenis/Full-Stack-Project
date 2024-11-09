import React from 'react';
import './App.css';
import ListComponent from './components/ListComponent';
import CarComponent from './components/CarComponent';
import ViewCarImageComponent from './components/ViewCarImageComponent';
import Footer from './layout/Footer';
import NavBar from './layout/NavBar';
import Home from './home/Home';
import LoginComponent from './components/LoginComponent';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import CarGrid from './components/CarGrid';
import CarDetails from './components/CarDetails';
import Conditii from './components/Conditii';

function App() {
  return (
    <div className="App">
      <BrowserRouter>
        <NavBar />
        <Routes>
          <Route path='/' element={<ListComponent />} />
          <Route path='/car' element={<ListComponent />} />
          <Route path='/add-car' element={<CarComponent />} />
          <Route path='/edit-car/:id' element={<CarComponent />} />
          <Route path='/home' element={<Home />} />
          <Route path='/login' element={<LoginComponent />} />
          <Route path='/cars' element={<CarGrid />} />
          <Route path="/view-car-image/:id" element={<ViewCarImageComponent />} />
          <Route path="/email" element={<CarDetails />} />
          <Route path='/conditii' element={<Conditii />} />
          <Route path='/rezervari' element={<CarDetails />} />
        </Routes>
        <Footer />
      </BrowserRouter>
    </div>
  );
}

export default App;
