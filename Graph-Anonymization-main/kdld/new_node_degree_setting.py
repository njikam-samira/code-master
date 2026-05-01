import networkx as nx
from typing import List, Tuple, Dict

def new_node_degree_setting(G: nx.Graph, P: List[Tuple[int, int]], P_new: List[Tuple[int, int]]) -> nx.Graph:
    """
    Ajuste les degrés des noisy nodes pour qu'ils atteignent un degré cible dans P_new tout en préservant l'APL.

    Args:
        G: Graphe NetworkX non orienté.
        P: Liste de tuples (id, degree) représentant les degrés actuels du graphe original.
        P_new: Liste de tuples (id, degree) représentant les degrés cibles.

    Returns:
        Graphe modifié G.
    """
    
    G = G.copy()

    # Identifier les noisy nodes (ID >= len(P))
    original_node_count = len(P)
    noisy_nodes = [n for n in G.nodes() if n >= original_node_count]
    #print(f"Liste de noisy nodes: {noisy_nodes}")

    # Créer des paires de noisy nodes à au plus 3 sauts
    pairs = []
    for i, u in enumerate(noisy_nodes):
        for v in noisy_nodes[i + 1:]:
            try:
                shortest_path_length = nx.shortest_path_length(G, u, v)
                if shortest_path_length <= 3:
                    pairs.append((u, v))
            except nx.NetworkXNoPath:
                continue
            
    #print(f"Liste des paires de noisy nodes: {pairs}")

    # Ajouter une arête pour chaque paire
    for u, v in pairs:
        if not G.has_edge(u, v):
            G.add_edge(u, v)

    # Ensemble des degrés dans P_new
    P_new_degrees = set(degree for _, degree in P_new)
    #print(f"Liste des targets: {P_new_degrees}")

    # Séparer les degrés pairs et impairs dans P_new
    even_degrees = sorted([d for d in P_new_degrees if d % 2 == 0])
    odd_degrees = sorted([d for d in P_new_degrees if d % 2 == 1])

    #print(f"Liste des targets pairs: {even_degrees}")
    #print(f"Liste des targets impairs: {odd_degrees}")

    # Ajuster les degrés des noisy nodes
    for n in noisy_nodes:
        #print(f"traitement du noeud {n}")
        n_degree = G.degree(n)

        # Sélectionner target_new en fonction de la parité
        if n_degree % 2 == 0: 
            # Choisir le plus petit degré pair dans P_new supérieur à n_degree
            target_new = next((d for d in even_degrees if d > n_degree), None)
        else:  
            # Choisir le plus petit degré impair dans P_new supérieur à n_degree
            target_new = next((d for d in odd_degrees if d > n_degree), None)

        # Si aucun target_new n'est trouvé (par exemple, n_degree est déjà plus grand que tous les degrés dans P_new)
        if target_new is None:
            continue

        # Ajuster le degré de n jusqu'à atteindre target_new
        while G.degree(n) != target_new:
            #print(f"tant que {n} a un degre {G.degree(n)} inférieur au degré cible {target_new}")
            # Trouver une arête (u, v) avec la distance minimale
            min_dist = float('inf')
            best_edge = None
            n_neighbors = set(G.neighbors(n))
            for u, v in G.edges():
                if u == n or v == n:
                    continue  
                if u in n_neighbors or v in n_neighbors:
                    continue
                try:
                    dist_u_n = nx.shortest_path_length(G, u, n)
                    dist_v_n = nx.shortest_path_length(G, v, n)
                    dist = (dist_u_n + dist_v_n) / 2
                    if dist < min_dist:
                        min_dist = dist
                        best_edge = (u, v)
                except nx.NetworkXNoPath:
                    continue

            if best_edge is None:
                break  

            # Supprimer l'arête (u, v) et ajouter (n, u) et (n, v)
            u, v = best_edge
            G.remove_edge(u, v)
            G.add_edge(n, u)
            G.add_edge(n, v)

    return G