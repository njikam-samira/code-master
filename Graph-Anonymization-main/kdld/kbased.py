from typing import List, Tuple

def calculate_cluster_cost(cluster: List[Tuple[int, int]]) -> int:
    #Calcule le coût d'un cluster comme la somme des différences absolues entre les degrés et le degré moyen.
    if not cluster:
        return 0
    degrees = [node[1] for node in cluster]
    mean_degree = round(sum(degrees) / len(degrees))
    return sum(abs(degree - mean_degree) for degree in degrees)

def k_based(P: List[Tuple[int, int]], k: int) -> List[Tuple[int, int]]:
    """
    Implémente l'algorithme K-L-BASED pour générer une séquence satisfaisant k-degree anonymity,
    mais sans prendre en compte la l-diversity.

    Args:
        P: Liste de tuples (id, degree) triée par degré décroissant.
        k: Paramètre pour k-degree anonymity.

    Returns:
        Liste de tuples (id, degree) représentant la séquence anonymisée.  
    """
    # Vérification des paramètres
    if k < 1:
        raise ValueError("k doit être positif")
    if len(P) < k:
        raise ValueError("La séquence P doit contenir au moins k nœuds")

    result = []
    i = 0
    #"""""
    while i < len(P):
        # Prendre au moins k nœuds pour former un cluster
        cluster = P[i:i+k]
        if len(cluster) < k:
            # Si moins de k nœuds restent, les fusionner dans le dernier cluster
            cluster = P[i:]
            mean_degree = round(sum(node[1] for node in cluster) / len(cluster))
            for node in cluster:
                result.append((node[0], mean_degree))
            break

        # Initialiser le degré moyen pour le cluster
        mean_degree = round(sum(node[1] for node in cluster) / len(cluster))

        # Comparer les coûts pour étendre le cluster
        j = i + k
        while j < len(P):
            current_cost = calculate_cluster_cost(cluster)

            # Option 1: Créer un nouveau cluster avec les k nœuds suivants
            new_cluster = P[j:j+k] if j + k <= len(P) else []
            C_new = float('inf')
            if len(new_cluster) >= k:
                C_new = current_cost + calculate_cluster_cost(new_cluster)

            # Option 2: Fusionner le nœud suivant et créer un nouveau cluster
            C_merge = float('inf')
            if j + 1 < len(P):
                extended_cluster = cluster + [P[j]]
                next_new_cluster = P[j+1:j+1+k] if j+1+k <= len(P) else []
                if len(next_new_cluster) >= k:
                    C_merge = calculate_cluster_cost(extended_cluster) + calculate_cluster_cost(next_new_cluster)

            # Choisir l'option avec le coût le plus faible
            if C_merge < C_new:
                cluster = extended_cluster
                mean_degree = round(sum(node[1] for node in cluster) / len(cluster))
                j += 1
            else:
                break

        # Ajouter les nœuds du cluster au résultat avec le degré moyen
        for node in cluster:
            result.append((node[0], mean_degree))

        i = j
    #"""""

    return result