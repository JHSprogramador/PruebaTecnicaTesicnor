package com.pruebatesicnor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

        while (true) {
            System.out.println("Seleccione una opción:");
            System.out.println("1. Agregar películas por nombre");
            System.out.println("2. Agregar todas las películas de Harry Potter");
            System.out.println("3. Agregar películas por nombre y rango de años");
            System.out.println("4. Mostrar todas las películas");
            System.out.println("5. Filtrar películas por título");
            System.out.println("6. Salir");
            int opcion = readLine.nextInt();

            readLine.nextLine(); // consume the newline

            switch (opcion) {
                case 1:
                    String nombrePelicula = "";
                    while (true) {
                        System.out.println("Ingrese el nombre de la pelicula (o 'salir' para terminar): ");
                        nombrePelicula = readLine.nextLine();
                        if (nombrePelicula.equalsIgnoreCase("salir")) {
                            break;
                        }
                        if (ComprobarPelicula(nombrePelicula)) {
                            Movie movie = getMovieInfo(nombrePelicula);
                            GuardarPeliculasEnDB(movie);
                        } else {
                            System.out.println(
                                    "La pelicula no esta en la base de datos de imdb y no puede ser introducida en la base de datos de movies_db");
                        }
                    }
                    break;
                case 2:
                    List<String> movieTitles = Arrays.asList("Harry Potter and the Philosopher's Stone",
                            "Harry Potter and the Chamber of Secrets", "Harry Potter and the Prisoner of Azkaban",
                            "Harry Potter and the Goblet of Fire", "Harry Potter and the Order of the Phoenix",
                            "Harry Potter and the Half-Blood Prince", "Harry Potter and the Deathly Hallows – Part 1",
                            "Harry Potter and the Deathly Hallows – Part 2");
                    for (String movieTitle : movieTitles) {
                        if (ComprobarPelicula(movieTitle)) {
                            Movie movie = getMovieInfo(movieTitle);
                            GuardarPeliculasEnDB(movie);
                        }
                    }
                    break;
                case 3:
                    System.out.println("Ingrese el nombre de la pelicula: ");
                    String nombrePeliculaConAño = readLine.nextLine();
                    System.out.println("Ingrese el año inicial: ");
                    int añoInicial = readLine.nextInt();
                    System.out.println("Ingrese el año final: ");
                    int añoFinal = readLine.nextInt();
                    for (int año = añoInicial; año <= añoFinal; año++) {
                        if (ComprobarPelicula(nombrePeliculaConAño, año)) {
                            Movie movie = getMovieInfo(nombrePeliculaConAño, año);
                            GuardarPeliculasEnDB(movie);
                        }
                    }
                    break;
                case 4:
                    mostrarTodasLasPeliculas();
                    break;
                case 5:
                    System.out.println("Ingrese el título de la película: ");
                    String titulo = readLine.nextLine();
                    filtrarPeliculasPorTitulo(titulo);
                    break;
                case 6:
                    System.out.println("Saliendo del programa...");

                    System.exit(0);
                    break;
                default:
                    readLine.close();
                    System.out.println("Opción no válida");
                    break;

            }

        }

    }

    // metodo que compruebaa si ña pelicula que se quiere introducir en la base de
    // OMDb
    public static boolean ComprobarPelicula(String movieTitle) throws Exception {
        return ComprobarPelicula(movieTitle, -1);
    }

    public static boolean ComprobarPelicula(String movieTitle, int year) throws Exception {
        Movie movie = getMovieInfo(movieTitle, year);
        return movie.imdbID != null;
    }

    public static Movie getMovieInfo(String movieTitle) throws Exception {
        return getMovieInfo(movieTitle, -1);
    }

    public static Movie getMovieInfo(String movieTitle, int year) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String uri = API_URL + "?apikey=" + API_KEY + "&t=" + movieTitle.replace(" ", "+");
        if (year != -1) {
            uri += "&y=" + year;
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(uri))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        Movie movie = mapper.readValue(response.body(), Movie.class);
        if (movie.imdbID != null) {
            movie.PersonalRating = new Random().nextInt(5) + 1;
        }

        return movie;
    }

    public static boolean peliculaExisteEnDB(String imdbID) throws Exception {
        String url = "jdbc:mysql://localhost:3306/movies_db";
        String usuario = "Pruebas";
        String pass = "bGB]!*7/P8yCvS(S";

        Connection connection = DriverManager.getConnection(url, usuario, pass);

        String selectSql = "SELECT imdbID FROM movies WHERE imdbID = ?";
        PreparedStatement selectStatement = connection.prepareStatement(selectSql);
        selectStatement.setString(1, imdbID);

        ResultSet resultSet = selectStatement.executeQuery();
        return resultSet.next();
    }

    public static void GuardarPeliculasEnDB(Movie movie) throws Exception {
        String url = "jdbc:mysql://localhost:3306/movies_db";
        String usuario = "Pruebas";
        String pass = "bGB]!*7/P8yCvS(S";
        if (peliculaExisteEnDB(movie.imdbID)) {
            System.out.println("La película" + movie.Title + " ya existe en la base de datos Movies_db. Omitiendo...");
            return;
        }
        PrintePeliculas(movie);
        Connection connection = DriverManager.getConnection(url, usuario, pass);

        // Insertar nueva película con los datos obtenidos
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

    public static void mostrarTodasLasPeliculas() throws Exception {
        String url = "jdbc:mysql://localhost:3306/movies_db";
        String usuario = "Pruebas";
        String pass = "bGB]!*7/P8yCvS(S";

        Connection connection = DriverManager.getConnection(url, usuario, pass);

        String selectSql = "SELECT * FROM movies";
        PreparedStatement selectStatement = connection.prepareStatement(selectSql);
        // ResultSet representa una tabla de datos generada al ejecutar una consulta
        // SQL contra una base de datos. Esencialmente actúa como un iterador para que
        // puedas recuperar las filas devueltas de la consulta SQL.
        ResultSet resultSet = selectStatement.executeQuery();
        // El método next() mueve el cursor a la siguiente fila, y dado que el cursor se
        // posiciona inicialmente antes de la primera fila,
        // la primera llamada al método next() hace que la primera fila sea la fila
        // actual.

        // El método next() devuelve true si la nueva fila actual es válida y false si
        // no hay más filas en el ResultSet.
        // Esto permite iterar sobre el ResultSet en un bucle while, procesando cada
        // fila de datos hasta que no haya más filas.

        // El método next() puede lanzar una SQLException si se produce un error de
        // acceso a la base de datos o si este método
        // se llama en un conjunto de resultados cerrado.
        // El objeto ResultSet debe cerrarse cuando hayas terminado con él para liberar
        // recursos.
        // Esto se hace típicamente en un bloque finally para asegurar que sucede
        // incluso si ocurre un error.
        while (resultSet.next()) {
            Movie movie = new Movie();
            movie.imdbID = resultSet.getString("imdbID");
            movie.Title = resultSet.getString("Title");
            movie.Year = resultSet.getString("Year");
            movie.Poster = resultSet.getString("Poster");
            movie.PersonalRating = resultSet.getInt("PersonalRating");

            PrintePeliculas(movie);
        }
    }

    public static void filtrarPeliculasPorTitulo(String titulo) throws Exception {
        String url = "jdbc:mysql://localhost:3306/movies_db";
        String usuario = "Pruebas";
        String pass = "bGB]!*7/P8yCvS(S";

        Connection connection = DriverManager.getConnection(url, usuario, pass);

        String selectSql = "SELECT * FROM movies WHERE Title = ?";
        PreparedStatement selectStatement = connection.prepareStatement(selectSql);
        selectStatement.setString(1, titulo);

        ResultSet resultSet = selectStatement.executeQuery();
        while (resultSet.next()) {
            Movie movie = new Movie();
            movie.imdbID = resultSet.getString("imdbID");
            movie.Title = resultSet.getString("Title");
            movie.Year = resultSet.getString("Year");
            movie.Poster = resultSet.getString("Poster");
            movie.PersonalRating = resultSet.getInt("PersonalRating");

            PrintePeliculas(movie);
        }
    }
}
