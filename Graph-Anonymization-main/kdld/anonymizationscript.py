import os
import networkx as nx
import numpy as np
import csv
import time
import pickle
from pathlib import Path
from kbased import k_based
from neighborhood_edge_editing import neighborhood_edge_editing
from Adding_Node_Decrease_Degree import adding_node_decrease_degree
from Adding_Node_Increase_Degree import adding_node_increase_degree
from new_node_degree_setting import new_node_degree_setting

# Chemins
KDLD_DATA_DIR = "data"
SECGRAPH_OUTPUT_DIR = "../secGraph/output"
KDLD_TIME_FILE = "execution_times.csv"

# Lister les fichiers .gpickle dans kdld/data
gpickle_files = [f for f in os.listdir(KDLD_DATA_DIR) if f.endswith(".gpickle")]

if not gpickle_files:
    print("Aucun fichier .gpickle trouvé dans kdld/data.")
    exit()

# Initialiser le fichier CSV pour les temps d'exécution
if not os.path.exists(KDLD_TIME_FILE):
    with open(KDLD_TIME_FILE, mode='w', newline='') as f:
        writer = csv.writer(f)
        writer.writerow(["Dataset", "k", "Method", "Execution_Time"])

# Liste des valeurs de k à tester
K_VALUES = list(range(5, 101, 5))
# Liste des datasets à ignorer pour la méthode union
IGNORE_DATASETS = ["Enron", "pubmed", "Wiki-Vote"]
# Méthode d'anonymisation
method = "KDLD"

for gpickle_file in gpickle_files:
    dataset_name = os.path.splitext(gpickle_file)[0]

    if dataset_name in IGNORE_DATASETS:
        print(f"On ignore l'anonymisation de {dataset_name} avec la méthode kdld.")
        continue
    
    input_path = os.path.join(KDLD_DATA_DIR, gpickle_file).replace(os.sep, '/')
    
    # Charger le graphe networkx
    with open(input_path, 'rb') as f:
        G_original = pickle.load(f)
    P_original = [(int(node), G_original.degree[node]) for node in G_original.nodes()]
    P_original = sorted(P_original, key=lambda x: x[1], reverse=True)

    for k in K_VALUES:
        print(f"\n=== Anonymisation de {dataset_name} avec k={k} ===")
        start_time = time.time()

        try:
            # Copier le graphe original
            G = G_original.copy()
            P = P_original.copy()
            
            # Étape 1 : K-BASED
            P_new = k_based(P, k)
            # Étape 2 : Neighborhood Edge Editing
            G_edited = neighborhood_edge_editing(G, P, P_new)
            P_prime = [(int(node), G_edited.degree[node]) for node in G_edited.nodes()]
            P_prime = sorted(P_prime, key=lambda x: x[1], reverse=True)
            # Étape 3 : Adding Node Decrease Degree
            G_after_decrease = adding_node_decrease_degree(G_edited, P, P_new)
            # Étape 4 : Adding Node Increase Degree
            G_after_increase = adding_node_increase_degree(G_after_decrease, P, P_new)
            # Étape 5 : New Node Degree Setting
            G_final = new_node_degree_setting(G_after_increase, P, P_new)

            # Préparer P_final
            P_final = [(int(node), G_final.degree[node]) for node in G_final.nodes()]
            P_final = sorted(P_final, key=lambda x: x[1], reverse=True)
            
            execution_time = time.time() - start_time

            # Sauvegarder le graphe anonymisé en format .pairs
            output_dir = os.path.join(SECGRAPH_OUTPUT_DIR, dataset_name, "KDLD").replace(os.sep, '/')
            Path(output_dir).mkdir(parents=True, exist_ok=True)
            output_pairs = os.path.join(output_dir, f"{dataset_name}KDLD{k}.pairs").replace(os.sep, '/')
            
            with open(output_pairs, 'w') as f:
                for edge in G_final.edges():
                    f.write(f"{edge[0]} {edge[1]}\n")
            
            # Enregistrer le temps d'exécution
            with open(KDLD_TIME_FILE, mode='a', newline='') as f:
                writer = csv.writer(f)
                writer.writerow([dataset_name, k, execution_time])

            print(f"Anonymisation terminée : Graphe sauvegardé dans {output_pairs}")

        except Exception as e:
            print(f"Erreur pour k={k} sur {dataset_name}: {e}")
            with open(KDLD_TIME_FILE, mode='a', newline='') as f:
                writer = csv.writer(f)
                writer.writerow([dataset_name, method, k, "error"])

print(f"\nTemps d'exécution sauvegardés dans {KDLD_TIME_FILE}")