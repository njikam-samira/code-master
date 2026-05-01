import networkx as nx
import torch
import numpy as np
from typing import List, Dict

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

def calculate_il(P_original: List[Dict[int, int]], P_new: List[Dict[int, int]]) -> float:
    il = np.sqrt(np.mean([(orig['degree'] - new['degree'])**2 for orig, new in zip(P_original, P_new)]))
    return il

def calculate_clustering_coefficient(G):
    #Calcule le Clustering Coefficient (CC) moyen d'un graphe.
    return nx.average_clustering(G)

def calculate_edge_intersection(G_original, G_anonimised):
    #Calcule l'Edge Intersection (EI) entre le graphe original et le graphe anonymisé.

    original_edges = set(G_original.edges())
    anonymized_edges = set(G_anonimised.edges())
    
    if not original_edges: 
        return 0.0
    
    intersection = len(original_edges.intersection(anonymized_edges))
    ei = intersection / len(original_edges)
    return ei

def verify_k_degree_anonymity(P_new, k: int) -> bool:
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
