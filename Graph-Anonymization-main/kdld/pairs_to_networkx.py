import os
import networkx as nx
import pickle
from pathlib import Path

# Chemin de base
KDLD_DATA_DIR = "data"

# Créer le dossier data s'il n'existe pas
Path(KDLD_DATA_DIR).mkdir(parents=True, exist_ok=True)

# Parcourir tous les fichiers dans kdld/data/
for graph_file in os.listdir(KDLD_DATA_DIR):
    if graph_file.endswith(".pairs"):
        # Chemin du fichier .pairs d'entrée
        input_pairs = os.path.join(KDLD_DATA_DIR, graph_file).replace(os.sep, '/')
        
        # Nom du fichier de sortie (ex. cora.pairs -> cora.gpickle)
        dataset_name = os.path.splitext(graph_file)[0]
        output_gpickle = os.path.join(KDLD_DATA_DIR, f"{dataset_name}.gpickle").replace(os.sep, '/')
        
        # Créer un graphe networkx
        G = nx.Graph()
        
        try:
            with open(input_pairs, 'r') as f:
                for line in f:
                    line = line.strip()
                    if line and not line.startswith('#'): 
                        source, target = map(int, line.split())
                        G.add_edge(source, target)
            
            # Sauvegarder le graphe en format gpickle
            with open(output_gpickle, 'wb') as f:
                pickle.dump(G, f, pickle.HIGHEST_PROTOCOL)
            print(f"Conversion terminée : {input_pairs} -> {output_gpickle}")
        
        except Exception as e:
            print(f"Erreur lors de la conversion de {input_pairs} en {output_gpickle}: {e}")
            continue

print("Conversion de tous les graphes terminée.")