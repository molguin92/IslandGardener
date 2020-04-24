package org.molguin.flowers;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class FlowerFactory {
    private final Map<FlowerConstants.Species, Graph> speciesGraphMap;

    public FlowerFactory(JsonObject flower_json) {
        // on instantiation, read the JSON data and set up internal map of flowers
        this.speciesGraphMap = new HashMap<>();
        for (FlowerConstants.Species species : FlowerConstants.Species.values()) {
            Graph species_graph = new DefaultGraph(
                    species.name(), false, true);
            species_graph.setNullAttributesAreErrors(true);
            this.speciesGraphMap.put(species, species_graph);
        }

        for (JsonObject.Member species_mb : flower_json) {
            // this for loop populates the map with the species, genotypes and associated colors and origins.
            FlowerConstants.Species species = FlowerConstants.Species.valueOf(species_mb.getName());
            Graph species_graph = speciesGraphMap.get(species);

            for (JsonObject.Member encoding_mb : species_mb.getValue().asObject()) {
                String encoding = encoding_mb.getName();
                JsonObject props = encoding_mb.getValue().asObject();
                String color = props.getString("color", null);
                String origin = props.getString("origin", null);

                Node node = species_graph.addNode(encoding);
                FlowerGenotype genotype = new FlowerGenotype(encoding);

                node.addAttribute("color", FlowerConstants.Color.valueOf(color));
                node.addAttribute("origin", FlowerConstants.Origin.valueOf(origin));
                node.addAttribute("genotype", genotype);
            }

            List<Node> nodes = new ArrayList<>(species_graph.getNodeSet());
            for (Node parent1 : nodes) {
                for (Node parent2 : nodes) {
                    String p1_id = parent1.getId();
                    String p2_id = parent2.getId();

                    String mating_id;
                    if (p1_id.compareTo(p2_id) >= 0)
                        mating_id = p1_id + "_" + p2_id;
                    else
                        mating_id = p2_id + "_" + p1_id;

                    if (species_graph.getNode(mating_id) != null) continue;

                    Node mating = species_graph.addNode(mating_id);
                    species_graph.addEdge(UUID.randomUUID().toString(),
                            parent1, mating, true);
                    species_graph.addEdge(UUID.randomUUID().toString(),
                            parent2, mating, true);

                    FlowerGenotype genes_p1 = parent1.getAttribute("genotype");
                    FlowerGenotype genes_p2 = parent2.getAttribute("genotype");

                    FlowerGenotype[] offspring = genes_p1.getAllPossibleOffspring(genes_p2);
                    for (FlowerGenotype child : offspring) {
                        // increase weight of edge from mating -> child
                        Node child_node = species_graph.getNode(child.encoded);
                        Edge edge = child_node.getEdgeFrom(mating_id);
                        // if edge doesn't exist, add it.
                        if (edge == null) {
                            edge = species_graph.addEdge(UUID.randomUUID().toString(),
                                    mating, child_node, true);
                            edge.setAttribute("count", 0);
                            edge.setAttribute("probability", 0.0d);
                        }
                        int new_count = edge.getAttribute("count", Integer.class) + 1;
                        double new_prob = new_count / (double) offspring.length;
                        edge.changeAttribute("count", new_count);
                        edge.changeAttribute("probability", new_prob);
                    }
                }
            }
        }
    }

    private Map<String, Double> getChildrenProbability(FlowerConstants.Species species,
                                                       String parent1, String parent2) {

        Graph species_graph = this.speciesGraphMap.get(species);
        Map<String, Double> results = new LinkedHashMap<>();

        String mating_id;
        if (parent1.compareTo(parent2) >= 0)
            mating_id = parent1 + "_" + parent2;
        else
            mating_id = parent2 + "_" + parent1;

        Node mating = species_graph.getNode(mating_id);
        for (Edge edge_to_child : mating.getEachLeavingEdge()) {
            Node child = edge_to_child.getTargetNode();
            String child_encoding = child.getId();
            double child_prob = edge_to_child.getAttribute("probability");

            results.put(child_encoding, child_prob);
        }

        return results;
    }

    public static void main(String[] args) throws IOException {
        String path = args[0];
        JsonValue json = Json.parse(
                new FileReader(path));

        FlowerFactory factory = new FlowerFactory(json.asObject());
        // test mating
        Map<String, Double> children =
                factory.getChildrenProbability(FlowerConstants.Species.ROSE, "2222", "2222");
        for (Map.Entry<String, Double> entry : children.entrySet())
            System.out.println(entry.getKey() + " " + entry.getValue());
    }
}
