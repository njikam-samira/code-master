import os
import subprocess
import time
import csv
from pathlib import Path

# Chemins de base
DATA_DIR = "data"
UMGA_JAR = "UMGA.jar"

# Valeurs de k (de 5 à 100, pas de 5)
K_VALUES = list(range(5, 101, 5))
# Liste des datasets à ignorer pour la méthode union
IGNORE_DATASETS = ["Enron", "pubmed", "Wiki-Vote"]
# Fichier CSV pour stocker les temps d'exécution
CSV_FILE = "temps_umga.csv"

# Initialiser le fichier CSV avec les en-têtes s'il n'existe pas
if not os.path.exists(CSV_FILE):
    with open(CSV_FILE, mode='w', newline='') as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow(["Dataset", "Method", "k", "Time (seconds)"])

# Parcourir tous les sous-dossiers dans le dossier data
for dataset_name in os.listdir(DATA_DIR):
    if dataset_name in IGNORE_DATASETS:
        print(f"On ignore l'anonymisation de {dataset_name} avec la méthode kdld.")
        continue

    dataset_dir = os.path.join(DATA_DIR, dataset_name).replace(os.sep, '/')
    if not os.path.isdir(dataset_dir):
        continue
    
    # Vérifier si le fichier .gml existe (ex. data/cora/cora.gml)
    input_graph = os.path.join(dataset_dir, f"{dataset_name}.gml").replace(os.sep, '/')
    if not os.path.exists(input_graph):
        print(f"Fichier GML {input_graph} non trouvé. Passage au suivant.")
        continue
    
    # Méthode d'anonymisation
    method = "UMGA"
    
    # Pour chaque valeur de k
    for k in K_VALUES:
        # Construire la commande Java avec plus de mémoire
        command = [
            "java", "-jar", UMGA_JAR,
            input_graph,
            str(k),
            "G",
            "NC"
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