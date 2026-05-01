import os
from pathlib import Path

# Chemin de base
DATA_DIR = "data"

# Parcourir tous les fichiers dans le dossier data
for graph_file in os.listdir(DATA_DIR):
    if graph_file.endswith(".pairs"):
        # Extraire le nom du dataset (ex. "cora" de "cora.pairs")
        dataset_name = os.path.splitext(graph_file)[0]
        
        # Chemin du fichier .pairs
        input_pairs = os.path.join(DATA_DIR, graph_file).replace(os.sep, '/')
        
        # Créer le dossier data/nomgraphe s'il n'existe pas
        dataset_dir = os.path.join(DATA_DIR, dataset_name).replace(os.sep, '/')
        if not os.path.exists(dataset_dir):
            os.makedirs(dataset_dir)
        
        # Chemin du fichier .gml de sortie (ex. data/cora/cora.gml)
        output_gml = os.path.join(dataset_dir, f"{dataset_name}.gml").replace(os.sep, '/')
        
        # Lire le fichier .pairs et collecter les nœuds et arêtes
        nodes = set()
        edges = []
        try:
            with open(input_pairs, 'r') as f:
                for line in f:
                    if line.strip():
                        edge = line.strip().split()
                        if len(edge) >= 2:
                            source, target = edge[0], edge[1]
                            nodes.add(source)
                            nodes.add(target)
                            edges.append((source, target))
        except Exception as e:
            print(f"Erreur lors de la lecture de {input_pairs}: {e}")
            continue
        
        # Écrire le fichier .gml
        try:
            with open(output_gml, 'w') as f:
                f.write("graph [\n")
                f.write("  directed 0\n")  # Graphe non dirigé
                
                # Écrire tous les nœuds
                node_id_map = {node: idx for idx, node in enumerate(sorted(nodes))}
                for node in sorted(nodes):
                    f.write(f"  node [\n    id {node_id_map[node]}\n  ]\n")
                
                # Écrire toutes les arêtes
                for source, target in edges:
                    f.write(f"  edge [\n    source {node_id_map[source]}\n    target {node_id_map[target]}\n  ]\n")
                
                f.write("]\n")
            print(f"Conversion terminée : {input_pairs} -> {output_gml}")
        except Exception as e:
            print(f"Erreur lors de l'écriture de {output_gml}: {e}")
            continue

print("Conversion de tous les graphes terminée.")