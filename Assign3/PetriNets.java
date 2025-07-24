import java.io.*;
import java.util.*;

class Place {
    String name;
    int tokens;

    Place(String name, int tokens) {
        this.name = name;
        this.tokens = tokens;
    }
}

class Transition {
    String name;
    List<String> inputPlaces = new ArrayList<>();
    List<String> outputPlaces = new ArrayList<>();

    Transition(String name) {
        this.name = name;
    }

    void addInput(String placeName) {
        inputPlaces.add(placeName);
    }

    void addOutput(String placeName) {
        outputPlaces.add(placeName);
    }

    boolean isEnabled(Map<String, Place> places) {
        for (String input : inputPlaces) {
            if (!places.containsKey(input) || places.get(input).tokens == 0) {
                return false;
            }
        }
        return true;
    }

    void fire(Map<String, Place> places) {
        for (String input : inputPlaces) {
            places.get(input).tokens--;
        }
        for (String output : outputPlaces) {
            places.get(output).tokens++;
        }
    }
}

public class PetriNets {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        BufferedWriter writer = new BufferedWriter(new FileWriter("scenario_output.txt"));

        Map<String, Place> places = new LinkedHashMap<>();
        List<Transition> transitions = new ArrayList<>();

        int numPlaces = 0;
        while (true) {
            try {
                System.out.print("Enter number of places: ");
                numPlaces = Integer.parseInt(scanner.nextLine());
                if (numPlaces <= 0) throw new NumberFormatException();
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid positive integer.");
            }
        }

        for (int i = 0; i < numPlaces; i++) {
            while (true) {
                try {
                    System.out.print("Enter place name and initial token count (e.g., p1 1): ");
                    String[] parts = scanner.nextLine().split(" ");
                    if (parts.length != 2) throw new IllegalArgumentException();
                    String placeName = parts[0];
                    if (places.containsKey(placeName)) {
                        System.out.println("Place name already used. Please enter a unique name.");
                        continue;
                    }
                    int tokens = Integer.parseInt(parts[1]);
                    if (tokens < 0) throw new NumberFormatException();
                    places.put(placeName, new Place(placeName, tokens));
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Token count must be a non-negative integer.");
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid input. Please enter in the format: name count (e.g., p1 1)");
                }
            }
        }

        int numTransitions = 0;
        while (true) {
            try {
                System.out.print("Enter number of transitions: ");
                numTransitions = Integer.parseInt(scanner.nextLine());
                if (numTransitions <= 0) throw new NumberFormatException();
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid positive integer.");
            }
        }

        for (int i = 0; i < numTransitions; i++) {
            System.out.print("Enter transition name: ");
            Transition t = new Transition(scanner.nextLine().trim());

            System.out.print("Enter input places (space-separated): ");
            String[] inputs = scanner.nextLine().trim().split(" ");
            for (String input : inputs) {
                if (!input.isEmpty()) t.addInput(input);
            }

            System.out.print("Enter output places (space-separated): ");
            String[] outputs = scanner.nextLine().trim().split(" ");
            for (String output : outputs) {
                if (!output.isEmpty()) t.addOutput(output);
            }

            transitions.add(t);
        }

        int step = 1;
        int maxSteps = 1000;

        while (step <= maxSteps) {
            StringBuilder currentMarking = new StringBuilder("Markings of the Petri Net <");
            for (Place p : places.values()) {
                currentMarking.append(p.name).append("=").append(p.tokens).append(", ");
            }
            currentMarking.setLength(currentMarking.length() - 2);
            currentMarking.append(">\n");
            writer.write(currentMarking.toString());
            System.out.print(currentMarking);

            List<Transition> enabled = new ArrayList<>();
            for (Transition t : transitions) {
                if (t.isEnabled(places)) {
                    enabled.add(t);
                }
            }

            if (enabled.isEmpty()) {
                writer.write("No transitions are enabled. Deadlock detected.\n");
                System.out.println("No transitions are enabled. Deadlock detected.");
                break;
            }

            boolean conflictDetected = false;
            Set<String> allUsedInputs = new HashSet<>();
            for (int i = 0; i < enabled.size(); i++) {
                for (int j = i + 1; j < enabled.size(); j++) {
                    Set<String> intersection = new HashSet<>(enabled.get(i).inputPlaces);
                    intersection.retainAll(enabled.get(j).inputPlaces);
                    if (!intersection.isEmpty()) {
                        conflictDetected = true;
                        break;
                    }
                }
            }

            if (conflictDetected) {
                System.out.print("Conflict detected between transitions: ");
                for (int i = 0; i < enabled.size(); i++) {
                    System.out.print(enabled.get(i).name);
                    if (i != enabled.size() - 1) System.out.print(", ");
                }
                System.out.println();
                System.out.print("Enter the transition to fire: ");
                String choice = scanner.nextLine().trim();
                boolean found = false;
                for (Transition t : enabled) {
                    if (t.name.equals(choice)) {
                        t.fire(places);
                        found = true;
                        writer.write("Transition(s) " + choice + " are enabled and so fired.\n");
                        break;
                    }
                }
                if (!found) {
                    System.out.println("Invalid transition choice. Skipping all conflicting transitions.");
                    writer.write("No transition fired due to invalid choice.\n");
                }
            } else {
                List<String> firedNames = new ArrayList<>();
                for (Transition t : enabled) {
                    t.fire(places);
                    firedNames.add(t.name);
                }
                writer.write("Transition(s) " + String.join(" and ", firedNames) + " are enabled and so fired.\n");
                System.out.println("Transition(s) " + String.join(" and ", firedNames) + " are enabled and so fired.");
            }

            StringBuilder newMarking = new StringBuilder("Markings of the Petri Net <");
            for (Place p : places.values()) {
                newMarking.append(p.name).append("=").append(p.tokens).append(", ");
            }
            newMarking.setLength(newMarking.length() - 2);
            newMarking.append(">\n\n");
            writer.write(newMarking.toString());
            System.out.print(newMarking);

            step++;
        }

        writer.close();
        scanner.close();
    }
}