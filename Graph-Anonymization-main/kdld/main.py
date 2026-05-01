import networkx as nx
from torch_geometric.datasets import Planetoid
from torch_geometric.utils import to_networkx
import matplotlib.pyplot as plt
from kbased import k_based
from typing import List, Tuple
import numpy as np
from neighborhood_edge_editing import neighborhood_edge_editing
from Adding_Node_Decrease_Degree import adding_node_decrease_degree
from Adding_Node_Increase_Degree import adding_node_increase_degree
from new_node_degree_setting import new_node_degree_setting

def calculate_apl(G: nx.Graph) -> float:
    total_distance = 0
    num_pairs = 0
    nodes = list(G.nodes())
    for i in range(len(nodes)):
        for j in range(i + 1, len(nodes)):
            try:
                distance = nx.shortest_path_length(G, source=nodes[i], target=nodes[j])
                total_distance += distance
                num_pairs += 1
            except nx.NetworkXNoPath:
                pass
    if num_pairs == 0:
        return float('inf')

    n = G.number_of_nodes()
    apl = (2 * total_distance) / (n * (n - 1))
    return apl


def load_cora_graph() -> tuple[List[Tuple[int, int]], nx.Graph]:
    # Charge le dataset Cora et retourne la séquence P et le graphe NetworkX.
    # Charger Cora avec torch-geometric
    dataset = Planetoid(root='data/Cora', name='Cora')
    data = dataset[0]
    # Convertir en graphe NetworkX
    G = to_networkx(data, to_undirected=True)
    # Générer la séquence P (id, degree)
    P = [(int(node), G.degree[node]) for node in G.nodes()]
    # tri de P par degré décroissant
    P = sorted(P, key=lambda x: x[1], reverse=True)
    return P, G

def calcule_il(P_original: List[Tuple[int, int]], P_new: List[Tuple[int, int]]) -> int:
    il = np.sqrt(np.mean([(orig[1] - new[1])**2 for orig, new in zip(P_original, P_new)]))
    return il

def verify_k_degree_anonymity(P_new: List[Tuple[int, int]], k: int) -> bool:
    # Vérifie si la séquence P_new satisfait la k-degree anonymity
    # Compter les occurrences de chaque degré
    degree_counts = {}
    for _, degree in P_new:
        degree_counts[degree] = degree_counts.get(degree, 0) + 1
    
    # Vérifier que chaque degré apparaît au moins k fois
    for degree, count in degree_counts.items():
        if count < k:
            print(f"Violation de k-degree anonymity : le degré {degree} apparaît {count} fois (< {k})")
            return False
    return True

def plot_degree_distribution(P_original: List[Tuple[int, int]], P_new: List[Tuple[int, int]], P_prime: List[Tuple[int, int]]):
    orig_degrees = [node[1] for node in P_original]
    new_degrees = [node[1] for node in P_new]
    edited_degrees = [node[1] for node in P_prime]

    plt.figure(figsize=(15, 5))
    plt.subplot(1, 3, 1)
    plt.hist(orig_degrees, bins=30, color='blue', alpha=0.7)
    plt.title("Distribution des degrés (Original)")
    plt.xlabel("Degré")
    plt.ylabel("Fréquence")

    plt.subplot(1, 3, 2)
    plt.hist(new_degrees, bins=30, color='green', alpha=0.7)
    plt.title("Distribution des degrés (Anonymisée)")
    plt.xlabel("Degré")
    plt.ylabel("Fréquence")

    plt.subplot(1, 3, 3)
    plt.hist(edited_degrees, bins=30, color='red', alpha=0.7)
    plt.title("Distribution des degrés (Après édition)")
    plt.xlabel("Degré")
    plt.ylabel("Fréquence")

    plt.tight_layout()
    plt.savefig('degree_distribution.png')
    plt.close()

k = 20
# Charger le dataset Cora
print("Chargement du dataset Cora...")
P, G = load_cora_graph()
print(f"Nombre de nœuds dans G: {len(G.nodes())}")
print(f"Nombre de nœuds dans P: {len(P)}")
print(f"Nombre d'arêtes: {G.number_of_edges()}")

print(f"Le graphe G est il connexe?: {nx.is_connected(G)}")
print(f"Nombre de noeud du plus grand composant connexe: {len(max(nx.connected_components(G), key=len))}")

#print(f"Sequence KD: {P}")
print(" ")
# Appliquer l'algorithme K-BASED
P_new = k_based(P, k)
print(f"Nouvelle sequence KD: {P_new}")
"""""
print("\nApplication de Neighborhood_Edge_Editing...")
G_edited = neighborhood_edge_editing(G, P, P_new)
print(f"Nombre d'arêtes après édition: {G_edited.number_of_edges()}")
P_prime = [(int(node), G_edited.degree[node]) for node in G_edited.nodes()]
P_prime = sorted(P_prime, key=lambda x: x[1], reverse=True)
print(f"Sequence KD édité: {P_prime}")

print("\nApplication de Adding_Node_Decrease_Degree...")
G_after_decrease = adding_node_decrease_degree(G_edited, P, P_new)
print(f"Nombre de nœuds après Adding_Node_Decrease_Degree: {len(G_after_decrease.nodes())}")
print(f"Nombre d'arêtes après Adding_Node_Decrease_Degree: {G_after_decrease.number_of_edges()}")

print("\nApplication de Adding_Node_Increase_Degree...")
G_after_increase = adding_node_increase_degree(G_after_decrease, P, P_new)
print(f"Nombre de nœuds après Adding_Node_Increase_Degree: {len(G_after_increase.nodes())}")
print(f"Nombre d'arêtes après Adding_Node_Increase_Degree: {G_after_increase.number_of_edges()}")

print("\nApplication de New_Node_Degree_Setting...")
G_final = new_node_degree_setting(G_after_increase, P, P_new)
print(f"Nombre de nœuds après New_Node_Degree_Setting: {len(G_final.nodes())}")
print(f"Nombre d'arêtes après New_Node_Degree_Setting: {G_final.number_of_edges()}")
"""""
