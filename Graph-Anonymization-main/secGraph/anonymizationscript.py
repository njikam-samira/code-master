import os
import subprocess
import time
import csv
from pathlib import Path

# Chemins de base
DATA_DIR = "data"
OUTPUT_DIR = "output"
SECGRAPH_JAR = "secGraphCLI.jar"

# Liste des méthodes d'anonymisation
ANONYMIZATION_METHODS = ["kDa", "tMean", "union"]

# Valeurs de k (de 5 à 100, pas de 5)
K_VALUES = list(range(5, 101, 5))
# Liste des datasets à ignorer pour la méthode union
UNION_IGNORE_DATASETS = ["Enron", "pubmed"]
# Fichier CSV pour stocker les temps d'exécution
CSV_FILE = "temps.csv"

# Créer le dossier de sortie s'il n'existe pas
if not os.path.exists(OUTPUT_DIR):
    os.makedirs(OUTPUT_DIR)

# Initialiser le fichier CSV avec les en-têtes s'il n'existe pas
if not os.path.exists(CSV_FILE):
    with open(CSV_FILE, mode='w', newline='') as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow(["Dataset", "Method", "k", "Time (seconds)"])

# Parcourir tous les fichiers dans le dossier data
for graph_file in os.listdir(DATA_DIR):
    if graph_file.endswith(".pairs"):
        # Extraire le nom du dataset (ex. "cora" de "cora.pairs")
        dataset_name = os.path.splitext(graph_file)[0]
        
        # Chemin du graphe d'entrée
        input_graph = os.path.join(DATA_DIR, graph_file).replace(os.sep, '/')
        
        # Créer le dossier du dataset dans output (ex. output/cora)
        dataset_output_dir = os.path.join(OUTPUT_DIR, dataset_name)
        if not os.path.exists(dataset_output_dir):
            os.makedirs(dataset_output_dir)
        
        # Pour chaque méthode d'anonymisation
        for method in ANONYMIZATION_METHODS:
            if method == "union" and dataset_name in UNION_IGNORE_DATASETS:
                print(f"On ignore l'anonymisatio de {dataset_name} avec la méthode union.")
                continue
            # Créer le dossier pour la méthode (ex. output/cora/kDa)
            method_output_dir = os.path.join(dataset_output_dir, method)
            if not os.path.exists(method_output_dir):
                os.makedirs(method_output_dir)
            
            # Pour chaque valeur de k
            for k in K_VALUES:
                # Nom du fichier de sortie (ex. output/cora/kDa/coraKda5.pairs)
                output_graph = os.path.join(method_output_dir, f"{dataset_name}{method}{k}.pairs").replace(os.sep, '/')
                
                # Déterminer le paramètre à utiliser (-k, -f, ou -t) selon la méthode
                if method in ["kDa", "union"]:
                    param = "-k"
                elif method == "tMean":
                    param = "-t"
                else:
                    print(f"Méthode {method} non reconnue. Passage au suivant.")
                    continue
                
                # Construire la commande Java
                command = [
                    "java", "-Xmx8g", "-jar", SECGRAPH_JAR,  
                    "-m", "a",
                    "-a", method,
                    "-gA", input_graph,
                    "-gO", output_graph,
                    param, str(k)
                ]
                
                # Mesurer le temps d'exécution
                start_time = time.time()
                try:
                    result = subprocess.run(command, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
                except subprocess.CalledProcessError as e:
                    print(f"Erreur lors de l'anonymisation de {dataset_name} avec {method} et k={k}: {e}")
                    print(f"Message d'erreur (stderr) : {e.stderr}")
                    continue
                end_time = time.time()
                execution_time = end_time - start_time
                
                # Enregistrer le temps dans le fichier CSV
                with open(CSV_FILE, mode='a', newline='') as csvfile:
                    writer = csv.writer(csvfile)
                    writer.writerow([dataset_name, method, k, execution_time])
                
                print(f"Anonymisation terminée : {dataset_name}, méthode={method}, k={k}, temps={execution_time:.2f}s")

print("Anonymisation terminée pour tous les graphes.")