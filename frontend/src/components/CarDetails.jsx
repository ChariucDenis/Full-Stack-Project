import React, { useEffect, useState } from 'react';


function carDetails() {
  const [rezervari, setRezervari] = useState([]);

  useEffect(() => {
    fetch('http://localhost:8080/api/v1/rezervari')
      .then(response => response.json())
      .then(data => setRezervari(data))
      .catch(error => console.error('Error fetching data:', error));
  }, []);

  return (
    <div className="rezervariPage">
      <h1 className='titlu-rezervari'>Rezervări</h1> {/* Titlul este acum în afara chenarului */}
      <div className="rezervariApp">
        <table className="rezervariTable">
          <thead>
            <tr>
              <th>ID</th>
              <th>Car ID</th>
              <th>First Name</th>
              <th>Last Name</th>
              <th>Email</th>
              <th>Phone</th>
              <th>Address</th>
            </tr>
          </thead>
          <tbody>
            {rezervari.map((rezervare) => (
              <tr key={rezervare.id}>
                <td>{rezervare.id}</td>
                <td>{rezervare.carId}</td>
                <td>{rezervare.firstName}</td>
                <td>{rezervare.lastName}</td>
                <td>{rezervare.email}</td>
                <td>{rezervare.phone}</td>
                <td>{rezervare.address}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  
  );
}

export default carDetails;
