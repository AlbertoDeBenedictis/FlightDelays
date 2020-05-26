package it.polito.tdp.extflightdelays.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import it.polito.tdp.extflightdelays.model.Airline;
import it.polito.tdp.extflightdelays.model.Airport;
import it.polito.tdp.extflightdelays.model.Flight;
import it.polito.tdp.extflightdelays.model.Rotta;

public class ExtFlightDelaysDAO {

	public List<Airline> loadAllAirlines() {
		String sql = "SELECT * from airlines";
		List<Airline> result = new ArrayList<Airline>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				result.add(new Airline(rs.getInt("ID"), rs.getString("IATA_CODE"), rs.getString("AIRLINE")));
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}

	/**
	 * Carica gli elementi nella idMap degli aeroporti in modo smart
	 * @param idMap
	 */
	public void loadAllAirports(Map<Integer, Airport> idMap) {
		String sql = "SELECT * FROM airports";
		

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				if(!idMap.containsKey(rs.getInt("ID"))) {
				Airport airport = new Airport(rs.getInt("ID"), rs.getString("IATA_CODE"), rs.getString("AIRPORT"),
						rs.getString("CITY"), rs.getString("STATE"), rs.getString("COUNTRY"), rs.getDouble("LATITUDE"),
						rs.getDouble("LONGITUDE"), rs.getDouble("TIMEZONE_OFFSET"));
				
				idMap.put(airport.getId(), airport);
				}
				
			}

			conn.close();
			

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}

	public List<Flight> loadAllFlights() {
		String sql = "SELECT * FROM flights";
		List<Flight> result = new LinkedList<Flight>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Flight flight = new Flight(rs.getInt("ID"), rs.getInt("AIRLINE_ID"), rs.getInt("FLIGHT_NUMBER"),
						rs.getString("TAIL_NUMBER"), rs.getInt("ORIGIN_AIRPORT_ID"),
						rs.getInt("DESTINATION_AIRPORT_ID"),
						rs.getTimestamp("SCHEDULED_DEPARTURE_DATE").toLocalDateTime(), rs.getDouble("DEPARTURE_DELAY"),
						rs.getDouble("ELAPSED_TIME"), rs.getInt("DISTANCE"),
						rs.getTimestamp("ARRIVAL_DATE").toLocalDateTime(), rs.getDouble("ARRIVAL_DELAY"));
				result.add(flight);
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}

	public int getAirlinesNumber(Airport a) {
		
		
		String sql = "select count(distinct airline_id)as numero_com from flights where (origin_airport_id = ?) OR (destination_airport_id = ?)";
		
		int res = -1;
		
		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			
			st.setInt(1, a.getId());
			st.setInt(2, a.getId());
			
			ResultSet rs = st.executeQuery();

			// NON HO BISOGNO DEL WHILE PERCHè HO UNA SOLA RIGA
			if(rs.next()) {
				
				res = rs.getInt("numero_com");
			}

			conn.close();
			
		

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
		
		
		return res;
		
	}

	public List<Rotta> getRotte(Map<Integer, Airport> idMap) {
		

		String sql = "select origin_airport_id as origine, destination_airport_id as arrivo, count(*) as peso from flights group by origin_airport_id, destination_airport_id";

		List<Rotta> listaRotte = new ArrayList<>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Airport sorgente = idMap.get(rs.getInt("origine"));
				Airport destinazione = idMap.get(rs.getInt("arrivo"));
				
				if(sorgente!=null && destinazione != null) {

				Rotta coppia = new Rotta(sorgente, destinazione, rs.getInt("peso"));
				listaRotte.add(coppia);
				}else {
					System.err.print("ERRORE IN GET ROTTE");
				}

			}
			
			// occhio a ritornare sempre la lista FUORI DAL WHILE!
			conn.close();
			return listaRotte;

		} catch (SQLException e) {
			e.printStackTrace();
		}

	return null;
		
	}
}
