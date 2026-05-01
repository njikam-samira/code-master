import networkx as nx
from typing import List, Tuple, Dict
from neighborhood_edge_editing import get_two_hop_neighbors

def adding_node_increase_degree(G: nx.Graph, P: List[Tuple[int, int]], P_new: List[Tuple[int, int]]) -> nx.Graph:
    """
    Ajoute des nœuds fictifs pour augmenter les degrés des nœuds qui sont en dessous de leur degré cible.

    Args:
        G: Graphe NetworkX non orienté.
        P: Liste de tuples (id, degree) représentant les degrés actuels.
        P_new: Liste de tuples (id, degree) représentant les degrés cibles.

    Returns:
        Graphe modifié G.
    """

    current_degrees: Dict[int, int] = {node_id: G.degree[node_id] for node_id, _ in P}
    target_degrees: Dict[int, int] = {node_id: degree for node_id, degree in P_new}

    # Copier le graphe pour éviter les modifications non désirées
    G = G.copy()

    # Compteur pour les nouveaux nœuds
    new_node_id = max(G.nodes()) + 1 if G.nodes() else 0

    # Ensemble des degrés uniques dans P_new
    P_new_degrees = set(degree for _, degree in P_new)
    min_degree_in_P_new = min(P_new_degrees) if P_new_degrees else 0

    # Pour chaque nœud u qui doit augmenter son degré
    for u, _ in P:
        d = current_degrees[u]  # Degré actuel de u
        target = target_degrees[u]  # Degré cible de u

        # Calculer le nombre d'aretes manquantes
        increase_num = target - d
        if increase_num <= 0:
            continue

        # Boucle pour augmenter le degré de u
        i = 0
        while i < increase_num:
            # Créer un nouveau nœud bruyant n
            n = new_node_id
            G.add_node(n)
            new_node_id += 1

            # Connecter u à n
            G.add_edge(u, n)
            current_degrees[u] = G.degree(u)

            # Liste pour suivre les connexions de n afin de pouvoir supprimer la dernière au cas où
            connections = [(u, n)]

            # Connecter les voisins à 1 ou 2 sauts de u à n
            one_hop = set(G.neighbors(u))
            two_hop = get_two_hop_neighbors(G, u)
            neighbors = one_hop.union(two_hop) - {u, n}

            for v in neighbors:
                if v in target_degrees and G.degree(v) < target_degrees[v]:
                    G.add_edge(v, n)
                    connections.append((v, n))

            # Ajuster le degré de n
            while G.degree(n) not in P_new_degrees and G.degree(n) > min_degree_in_P_new:
                if not connections[1:]:  # S'assurer qu'il reste des connexions à supprimer (sauf (u, n))
                    break
                # Supprimer la dernière connexion
                last_v, _ = connections.pop()
                G.remove_edge(last_v, n)
                i -= 1

            i += 1

    return G