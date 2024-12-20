import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Scanner;

class CurrencyConverter {
    private static final String API_KEY = "0caeff5fd7288d303f7e36cf";
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/";
    private static final String[] SELECTED_CURRENCIES = {"ARS", "BOB", "BRL", "CLP", "COP", "USD"};
    private static final String BASE_CURRENCY = "USD"; // Moneda base

    public static void main(String[] args) {
        try {
            // Obtener tasas de cambio
            String jsonResponse = fetchExchangeRates(BASE_CURRENCY);

            // Filtrar y almacenar tasas de cambio
            JsonObject rates = filterCurrencies(jsonResponse);

            // Solicitar entrada del usuario
            try (Scanner scanner = new Scanner(System.in)) {
                boolean continueConversion = true;

                while (continueConversion) {
                    System.out.print("Ingrese el valor a convertir: ");
                    double amount = scanner.nextDouble();

                    System.out.print("Seleccione la moneda de origen (ARS, BOB, BRL, CLP, COP, USD): ");
                    String fromCurrency = scanner.next().toUpperCase();

                    System.out.print("Seleccione la moneda de destino (ARS, BOB, BRL, CLP, COP, USD): ");
                    String toCurrency = scanner.next().toUpperCase();

                    // Validar monedas seleccionadas
                    if (isValidCurrency(fromCurrency) && isValidCurrency(toCurrency)) {
                        // Realizar conversión
                        if (rates.has(fromCurrency) && rates.has(toCurrency)) {
                            double convertedValue = convertCurrency(amount, rates, fromCurrency, toCurrency);
                            System.out.printf("Resultado: %.2f %s = %.2f %s%n", amount, fromCurrency, convertedValue, toCurrency);
                        } else {
                            System.out.println("Monedas no válidas seleccionadas.");
                        }
                    } else {
                        System.out.println("Monedas no válidas seleccionadas.");
                    }

                    System.out.print("¿Desea realizar otra conversión? (s/n): ");
                    String response = scanner.next().toLowerCase();
                    if (!response.equals("s")) {
                        continueConversion = false;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static String fetchExchangeRates(String base) throws Exception {
        String url = API_URL + API_KEY + "/latest/" + base;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("User-Agent", "CurrencyConverter/1.0")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new Exception("Error en la solicitud. Código de estado: " + response.statusCode());
        }
    }

    private static JsonObject filterCurrencies(String jsonResponse) {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
        JsonObject conversionRates = jsonObject.getAsJsonObject("conversion_rates");

        JsonObject filteredRates = new JsonObject();
        for (String currency : SELECTED_CURRENCIES) {
            if (conversionRates.has(currency)) {
                filteredRates.addProperty(currency, conversionRates.get(currency).getAsDouble());
            }
        }

        System.out.println("Tasas de cambio filtradas: " + filteredRates);
        return filteredRates;
    }

    private static double convertCurrency(double amount, JsonObject rates, String fromCurrency, String toCurrency) {
        double fromRate = rates.get(fromCurrency).getAsDouble();
        double toRate = rates.get(toCurrency).getAsDouble();

        // Convertir a moneda base y luego a la moneda destino
        return (amount / fromRate) * toRate;
    }

    private static boolean isValidCurrency(String currency) {
        for (String validCurrency : SELECTED_CURRENCIES) {
            if (validCurrency.equals(currency)) {
                return true;
            }
        }
        return false;
    }
}
