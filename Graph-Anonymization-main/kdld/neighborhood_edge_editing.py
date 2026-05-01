import networkx as nx
from typing import List, Tuple, Dict

def get_two_hop_neighbors(G: nx.Graph, u: int) -> set:
    #Retourne l'ensemble des voisins à 2 sauts de u, excluant u et ses voisins directs
    one_hop = set(G.neighbors(u))
    two_hop = set()
    for v in one_hop:
        for w in G.neighbors(v):
            if w != u and w not in one_hop:
                two_hop.add(w)
    return two_hop

def has_short_alternative_path(G: nx.Graph, u: int, v: int, max_length: int = 2) -> bool:
    #Vérifie s'il existe un chemin de longueur <= max_length entre u et v après suppression de (u, v)
    G_temp = G.copy()
    if G_temp.has_edge(u, v):
        G_temp.remove_edge(u, v)
    try:
        length = nx.shortest_path_length(G_temp, u, v)
        return length <= max_length
    except nx.NetworkXNoPath:
        return False

def neighborhood_edge_editing(G: nx.Graph, P: List[Tuple[int, int]], P_new: List[Tuple[int, int]]) -> nx.Graph:
    """
    Ajuste les degrés du graphe G pour se rapprocher de P_new en modifiant les arêtes.

    Args:
        G: Graphe NetworkX non orienté.
        P: Liste de tuples (id, degree) représentant les degrés actuels.
        P_new: Liste de tuples (id, degree) représentant les degrés cibles.

    Returns:
        Graphe modifié G.
    """
    # dictionnaires pour un accès rapide aux degrés
    current_degrees: Dict[int, int] = {node_id: degree for node_id, degree in P}
    target_degrees: Dict[int, int] = {node_id: degree for node_id, degree in P_new}

    # Copier le graphe pour éviter les modifications non désirées
    G = G.copy()

    #print("traitement des noeuds cas 1")
    # Cas 1: Pour chaque nœud u dont le degré a besoin d'être augmenté
    for u, d in P:
        d_prime = target_degrees[u]
        missing_edge = d_prime - d
        if current_degrees[u] >= target_degrees[u]:
            continue  
        for i in range(missing_edge):  # Pour chaque unité de degré à augmenter
            neighbors = list(G.neighbors(u))
            action_taken = False
            for v in neighbors:
                # v doit avoir besoin de reduire son degré 
                if current_degrees[v] <= target_degrees[v]:
                    continue  
                # Trouver un nœud w tel que (v, w) existe
                v_neighbors = list(G.neighbors(v))
                for w in v_neighbors:
                    if w == u:
                        continue  
                    # Supprimer (v, w) et ajouter (u, w) 
                    G.remove_edge(v, w)
                    if not G.has_edge(u, w):
                        G.add_edge(u, w)
                    current_degrees[w] = G.degree(w)
                    current_degrees[u] = G.degree(u)
                    current_degrees[v] = G.degree(v)
                    action_taken = True
                    break
                if action_taken:
                    break
            if not action_taken:
                break  

    #print("traitement des noeuds cas 2")
    # Cas 2: Pour chaque nœud u dont le degré a besoin d'être augmenté
    for u, d in P:
        d_prime = target_degrees[u]
        if current_degrees[u] >= target_degrees[u]:
            continue 
        # Trouver tous les nœuds v qui doivent augmenter leur degré
        candidates = []
        for v, d_v in P:
            if v == u:
                continue
            if current_degrees[v] >= target_degrees[v]:
                continue  
            # Vérifier si u et v sont voisins à 2 sauts
            two_hop = get_two_hop_neighbors(G, u)
            if v in two_hop and not G.has_edge(u, v):
                candidates.append(v)
        if not candidates:
            continue  

        while current_degrees[u] < target_degrees[u] and candidates: 
            # Ajouter une arête au premier candidat valide
            v = candidates[0]
            G.add_edge(u, v)
            current_degrees[u] = G.degree(u)
            current_degrees[v] = G.degree(v)
            candidates.pop(0)

    #print("traitement des noeuds cas 3")
    # Cas 3: Pour chaque nœud u qui doit réduire son degré
    for u, d in P:
        d_prime = target_degrees[u]
        #print(f"traitement du noeud {u}")
        tried_neighbors = set()  # Ensemble des voisins déjà essayés
        while current_degrees[u] > target_degrees[u]:  
            #print(f"tant que {u} a un degre {current_degrees[u]} supérieur au degré cible {target_degrees[u]}")
            neighbors = list(G.neighbors(u))
            action_taken = False
            for v in neighbors:
                if v in tried_neighbors:  
                    continue
                if current_degrees[v] <= target_degrees[v]:
                    continue  
                # Supprimer l'arête (u, v)
                G.remove_edge(u, v)
                current_degrees[u] = G.degree(u)
                current_degrees[v] = G.degree(v)
                #print(f"arête supprimée entre {u} et {v}")
                # Vérifier si u et v ne sont PAS voisins à 2 sauts
                two_hop_neighbors = get_two_hop_neighbors(G, u)
                if v not in two_hop_neighbors:  
                    G.add_edge(u, v)
                    current_degrees[u] = G.degree(u)
                    current_degrees[v] = G.degree(v)
                    #print(f"arête rajoutée entre {u} et {v}")
                tried_neighbors.add(v)  # Ajouter v aux voisins essayés
                action_taken = True
                break
            if not action_taken:
                break  
            
    return G