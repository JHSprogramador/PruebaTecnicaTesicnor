package com.pruebatesicnor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
    private static final String API_KEY = "731e41f";
    private static final String API_URL = "http://www.omdbapi.com/";

    public static void main(String[] args) throws Exception {
        Scanner readLine = new Scanner(System.in);
        System.out.println("Seleccione una opción:");
        System.out.println("1. Agregar películas por nombre");
        System.out.println("2. Agregar todas las películas de Harry Potter");
        System.out.println("3. Agregar películas por nombre y rango de años");
        int opcion = readLine.nextInt();
        readLine.nextLine(); // consume the newline

        if (opcion == 1) {
            String nombrePelicula = "";
            while (true) {
                System.out.println("Ingrese el nombre de la pelicula (o 'salir' para terminar): ");
                nombrePelicula = readLine.nextLine();
                if (nombrePelicula.equalsIgnoreCase("salir")) {
                    break;
                }
                if (ComprobarPelicula(nombrePelicula, 0)) {
                    Movie movie = getMovieInfo(nombrePelicula, 0);
                    GuardarPeliculasEnDB(movie);
                    PrintePeliculas(movie);
                } else {
                    System.out.println(
                            "La pelicula no esta en la base de datos de imdb y no puede ser introducida en la base de datos de movies_db");
                }
            }
        } else if (opcion == 2) {
            List<String> movieTitles = Arrays.asList("Harry Potter and the Philosopher's Stone",
                    "Harry Potter and the Chamber of Secrets", "Harry Potter and the Prisoner of Azkaban",
                    "Harry Potter and the Goblet of Fire", "Harry Potter and the Order of the Phoenix",
                    "Harry Potter and the Half-Blood Prince", "Harry Potter and the Deathly Hallows – Part 1",
                    "Harry Potter and the Deathly Hallows – Part 2");
            for (String movieTitle : movieTitles) {
                if (ComprobarPelicula(movieTitle)) {
                    Movie movie = getMovieInfo(movieTitle);
                    GuardarPeliculasEnDB(movie);
                    PrintePeliculas(movie);
                }
            }
        } else if (opcion == 3) {
            System.out.println("Ingrese el nombre de la pelicula: ");
            String nombrePelicula = readLine.nextLine();
            System.out.println("Ingrese el año inicial: ");
            int añoInicial = readLine.nextInt();
            System.out.println("Ingrese el año final: ");
            int añoFinal = readLine.nextInt();
            for (int año = añoInicial; año <= añoFinal; año++) {
                if (ComprobarPelicula(nombrePelicula, año)) {
                    Movie movie = getMovieInfo(nombrePelicula, año);
                    GuardarPeliculasEnDB(movie);
                    PrintePeliculas(movie);
                }
            }
        } else {
            System.out.println("Opción no válida");
        }
    }

    // metodo que compruebaa si ña pelicula que se quiere introducir en la base de
    // OMDb
    public static boolean ComprobarPelicula(String movieTitle) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(API_URL + "?apikey=" + API_KEY + "&t=" + movieTitle.replace(" ", "+")))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        Movie movie = mapper.readValue(response.body(), Movie.class);
        if (movie.imdbID == null) {
            return false;
        }
        return true;
    }

    // metodo que compruebaa si ña pelicula que se quiere introducir en la base de
    // OMDb y hace uso del año
    public static boolean ComprobarPelicula(String movieTitle, int year) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(API_URL + "?apikey=" + API_KEY + "&t=" + movieTitle.replace(" ", "+") + "&y=" + year))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        Movie movie = mapper.readValue(response.body(), Movie.class);
        if (movie.imdbID == null) {
            return false;
        }
        return true;
    }

    // metodo que obtiene la informacion de la pelicula
    public static Movie getMovieInfo(String movieTitle) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(API_URL + "?apikey=" + API_KEY + "&t=" + movieTitle.replace(" ", "+")))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        Movie movie = mapper.readValue(response.body(), Movie.class);
        movie.PersonalRating = new Random().nextInt(5) + 1;

        return movie;
    }

    public static Movie getMovieInfo(String movieTitle, int year) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(API_URL + "?apikey=" + API_KEY + "&t=" + movieTitle.replace(" ", "+") + "&y=" + year))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        Movie movie = mapper.readValue(response.body(), Movie.class);
        movie.PersonalRating = new Random().nextInt(5) + 1;

        return movie;
    }

    public static void GuardarPeliculasEnDB(Movie movie) throws Exception {
        String url = "jdbc:mysql://localhost:3306/movies_db";
        String usuario = "Pruebas";
        String pass = "bGB]!*7/P8yCvS(S";

        Connection connection = DriverManager.getConnection(url, usuario, pass);

        // Insertar nuevos datos
        String insertSql = "INSERT INTO movies (imdbID, Title, Year, Poster, PersonalRating) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement insertStatement = connection.prepareStatement(insertSql);
        insertStatement.setString(1, movie.imdbID);
        insertStatement.setString(2, movie.Title);
        insertStatement.setString(3, movie.Year);
        insertStatement.setString(4, movie.Poster);
        insertStatement.setInt(5, movie.PersonalRating);

        int rowsInserted = insertStatement.executeUpdate();
        if (rowsInserted > 0) {
            System.out.println("A new movie was inserted successfully!");
        }
    }

    public static void PrintePeliculas(Movie movie) {
        System.out.println("IMDB ID: " + movie.imdbID);
        System.out.println("Title: " + movie.Title);
        System.out.println("Year: " + movie.Year);
        System.out.println("Poster URL: " + movie.Poster);
        System.out.println("Personal Rating: " + movie.PersonalRating);
        System.out.println("-----------------------------");
    }
}