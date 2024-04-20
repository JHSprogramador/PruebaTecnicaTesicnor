import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HarryPotterMovies {

    private static final String API_KEY = "731e41f";
    private static final String API_URL = "http://www.omdbapi.com/";

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        ObjectMapper mapper = new ObjectMapper();

        // Lista para almacenar información de las películas
        List<Movie> movies = new ArrayList<>();

        // Peticiones a la API de OMDb para cada película de Harry Potter
        for (String movieName : Arrays.asList("Harry Potter and the Sorcerer's Stone",
                "Harry Potter and the Chamber of Secrets",
                "Harry Potter and the Prisoner of Azkaban", "Harry Potter and the Goblet of Fire",
                "Harry Potter and the Order of the Phoenix",
                "Harry Potter and the Half-Blood Prince", "Harry Potter and the Deathly Hallows: Part 1",
                "Harry Potter and the Deathly Hallows: Part 2")) {
            String url = API_URL + "?apikey=" + API_KEY + "&t=" + movieName;
            HttpRequest request = HttpRequest.newBuilder(url).build();

            try (HttpResponse<String> response = HttpClient.newHttpClient().send(request,
                    HttpResponse.BodyHandlers.ofString())) {
                if (response.statusCode() == 200) {
                    Movie movie = mapper.readValue(response.body(), Movie.class);

                    // Solicitar valoración al usuario
                    System.out.print("Valoración personal (1-5) para " + movie.getTitle() + ": ");
                    int rating = scanner.nextInt();
                    scanner.nextLine(); // Consumir salto de línea

                    movie.setPersonalRating(rating);
                    movies.add(movie);
                } else {
                    System.err.println("Error al obtener información de la película: " + movieName);
                }
            }
        }
    }
}
