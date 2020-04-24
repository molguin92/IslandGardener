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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FlowerFactory {
    private final Map<FlowerConstants.Species, Graph> speciesGraphMap;

    public FlowerFactory(JsonObject flower_json) {
        // on instantiation, read the JSON data and set up internal map of flowers
        this.speciesGraphMap = new HashMap<>();
        for (FlowerConstants.Species species : FlowerConstants.Species.values()) {
            // prepare the internal map
            Graph species_graph = new DefaultGraph(
                    species.name(), false, true);
            species_graph.setNullAttributesAreErrors(true);
            this.speciesGraphMap.put(species, species_graph);
        }

        for (JsonObject.Member species_mb : flower_json) {
            // this for loop populates the map with the flowers.
            FlowerConstants.Species species = FlowerConstants.Species.valueOf(species_mb.getName());
            Graph species_graph = speciesGraphMap.get(species);

            for (JsonObject.Member encoding_mb : species_mb.getValue().asObject()) {
                String encoding = encoding_mb.getName();
                JsonObject props = encoding_mb.getValue().asObject();

                // get properties
                String color = props.getString("color", null);
                String origin = props.getString("origin", null);

                Node node = species_graph.addNode(encoding);
                node.addAttribute("flower", new Flower(species,
                        FlowerConstants.Color.valueOf(color),
                        FlowerConstants.Origin.valueOf(origin),
                        new FlowerGenotype(encoding)));
            }

            List<Node> nodes = new ArrayList<>(species_graph.getNodeSet());
            for (Node parent1 : nodes) {
                for (Node parent2 : nodes) {
                    // set up special nodes mapping matings between flowers to their offspring

                    String p1_id = parent1.getId();
                    String p2_id = parent2.getId();

                    // Always lexicographically order the ids to form the mating id
                    // this way p1 + p2 == p2 + p1
                    String mating_id;
                    if (p1_id.compareTo(p2_id) >= 0)
                        mating_id = p1_id + "_" + p2_id;
                    else
                        mating_id = p2_id + "_" + p1_id;

                    // if we already calculated this mating, skip
                    if (species_graph.getNode(mating_id) != null) continue;

                    Node mating = species_graph.addNode(mating_id);
                    // directed edges indicate mating and offspring direction
                    species_graph.addEdge(UUID.randomUUID().toString(),
                            parent1, mating, true);
                    species_graph.addEdge(UUID.randomUUID().toString(),
                            parent2, mating, true);

                    Flower flower_p1 = parent1.getAttribute("flower");
                    Flower flower_p2 = parent2.getAttribute("flower");
                    FlowerGenotype genes_p1 = flower_p1.genotype;
                    FlowerGenotype genes_p2 = flower_p2.genotype;

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

    public List<Flower> getAllFlowersForSpecies(FlowerConstants.Species species) {
        Graph species_g = this.speciesGraphMap.get(species);
        List<Flower> flowers = new LinkedList<>();

        for (Node node : species_g.getNodeSet())
            if (node.hasAttribute("flower"))
                flowers.add(node.getAttribute("flower"));

        return flowers;
    }

    public Map<Flower, Double> getChildrenProbability(Flower parent1, Flower parent2){
        if (parent1.species != parent2.species) return null;

        return this.getChildrenProbability(
                parent1.species,
                parent1.getEncodedGenotype(),
                parent2.getEncodedGenotype());
    }

    private Map<Flower, Double> getChildrenProbability(FlowerConstants.Species species,
                                                      String parent1, String parent2) {

        Graph species_graph = this.speciesGraphMap.get(species);
        Map<Flower, Double> results = new LinkedHashMap<>();

        String mating_id;
        if (parent1.compareTo(parent2) >= 0)
            mating_id = parent1 + "_" + parent2;
        else
            mating_id = parent2 + "_" + parent1;

        Node mating = species_graph.getNode(mating_id);
        for (Edge edge_to_child : mating.getEachLeavingEdge()) {
            Node child = edge_to_child.getTargetNode();
            Flower child_flower = child.getAttribute("flower");
            double child_prob = edge_to_child.getAttribute("probability");

            results.put(child_flower, child_prob);
        }

        return results;
    }

    public static void main(String[] args) throws IOException {
        String path = args[0];
        JsonValue json = Json.parse(
                new FileReader(path));

        FlowerFactory factory = new FlowerFactory(json.asObject());
        // test mating
        Map<Flower, Double> children =
                factory.getChildrenProbability(FlowerConstants.Species.ROSE,
                        "2222", "0000");
        for (Map.Entry<Flower, Double> entry : children.entrySet())
            System.out.println(entry.getKey().toString() + " " + entry.getValue());
    }
}
