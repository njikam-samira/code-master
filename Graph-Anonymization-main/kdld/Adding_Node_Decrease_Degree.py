import networkx as nx
from typing import List, Tuple, Dict
import random

def select_closest_degree_in_group(value: int, P_new: List[Tuple[int, int]]) -> int:
    """
    Trouve le degré dans P_new qui est le plus proche de la valeur donnée.

    Args:
        value: Valeur cible (u.d + 2 - pu.d).
        P_new: Liste de tuples (id, degree) représentant les degrés cibles.

    Returns:
        Degré dans P_new le plus proche de value.
    """
    degrees = sorted(set(degree for _, degree in P_new))  # Degrés uniques dans P_new
    return min(degrees, key=lambda x: abs(x - value))

def adding_node_decrease_degree(G: nx.Graph, P: List[Tuple[int, int]], P_new: List[Tuple[int, int]]) -> nx.Graph:
    """
    Ajoute des nœuds fictifs pour réduire les degrés des nœuds qui dépassent leur degré cible.

    Args:
        G: Graphe NetworkX non orienté.
        P: Liste de tuples (id, degree) représentant les degrés actuels.
        P_new: Liste de tuples (id, degree) représentant les degrés cibles.

    Returns:
        Graphe modifié G avec des nœuds fictifs ajoutés.
    """
    # Dictionnaires pour un accès rapide aux degrés
    current_degrees: Dict[int, int] = {node_id: G.degree[node_id] for node_id, _ in P}
    target_degrees: Dict[int, int] = {node_id: degree for node_id, degree in P_new}

    G = G.copy()

    # Compteur pour les nouveaux nœuds 
    new_node_id = max(G.nodes()) + 1 if G.nodes() else 0

    # Trouver le degré minimum dans P_new
    min_degree_in_P_new = min(degree for _, degree in P_new)

    # Pour chaque nœud u qui doit réduire son degré
    for u, _ in P:
        d = current_degrees[u]  # Degré actuel de u
        target = target_degrees[u]  # Degré cible de u

        # Tant que le degré de u est supérieur au degré cible
        while d > target:
            # Créer un nouveau nœud n avec la valeur sensible s
            n = new_node_id
            G.add_node(n)
            new_node_id += 1

            # Connecter u à n
            G.add_edge(u, n)
            current_degrees[u] = G.degree(u)
            d = current_degrees[u]
            d_prime = G.degree(n)  # Degré de 1

            # Calculer target_new qui est le dégré cible de notre noeud bruyant
            target_new_candidate = d + 2 - target
            if target_new_candidate < min_degree_in_P_new:
                target_new = min_degree_in_P_new
            else:
                target_new = select_closest_degree_in_group(target_new_candidate, P_new)

            # Tant que le degré de n n'a pas atteint target_new et que u doit encore réduire son degré
            while True:
                # Sélectionner aléatoirement une arête (u, v) dans G
                neighbors = list(G.neighbors(u))
                if not neighbors:
                    break  
                v = random.choice(neighbors)

                # Supprimer l'arête (u, v) et créer (n, v)
                G.remove_edge(u, v)
                if not G.has_edge(n, v):
                    G.add_edge(n, v)

                # Mettre à jour les degrés
                d_prime = G.degree(n)
                current_degrees[u] = G.degree(u)
                d = current_degrees[u]

                # Vérifier les conditions de sortie
                if d_prime == target_new or d == target:
                    break

            # Si le degré de u a atteint son objectif, sortir
            if d == target:
                break

    return G