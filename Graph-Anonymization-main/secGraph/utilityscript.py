import os
import subprocess
import csv
import re
from pathlib import Path
import networkx as nx
import numpy as np

# Chemins de base
DATA_DIR = "data"
OUTPUT_DIR = "output"
SECGRAPH_JAR = "secGraphCLI.jar"

# Liste des métriques d'utilité à calculer (d'après le Readme.txt)
UTILITY_METRICS = ["deg", "JD", "ED", "LCC", "CC", "BC", "EV", "NC", "Rolx", "Infl", "CD", "AS", "HS", "PR"]

# Fonction pour charger un graphe depuis un fichier .pairs
def load_graph_from_pairs(file_path: str) -> nx.Graph:
    G = nx.Graph()
    try:
        with open(file_path, 'r') as f:
            for line in f:
                if line.strip():
                    edge = line.strip().split()
                    if len(edge) >= 2:
                        G.add_edge(edge[0], edge[1])
        return G
    except Exception as e:
        print(f"Erreur lors du chargement de {file_path}: {e}")
        return nx.Graph()

# Fonctions pour calculer les métriques personnalisées
def calculate_apl(G: nx.Graph) -> float:
    total_distance = 0
    num_pairs = 0
    nodes = list(G.nodes())
    for i in range(len(nodes)):
        for j in range(i + 1, len(nodes)):
            try:
                distance = nx.shortest_path_length(G, source=nodes[i], target=nodes[j])
                total_distance += distance
                num_pairs += 1
            except nx.NetworkXNoPath:
                pass
    if num_pairs == 0:
        return float('inf')
    n = G.number_of_nodes()
    return (2 * total_distance) / (n * (n - 1))

def calculate_clustering_coefficient(G: nx.Graph) -> float:
    return nx.average_clustering(G)

def calculate_edge_intersection(G_original: nx.Graph, G_anonimised: nx.Graph) -> float:
    original_edges = set(G_original.edges())
    anonymized_edges = set(G_anonimised.edges())
    if not original_edges:
        return 0.0
    intersection = len(original_edges.intersection(anonymized_edges))
    return intersection / len(original_edges)

def calculate_ilrmse(G_original: nx.Graph, G_anonimised: nx.Graph) -> float:
    P_original = [{'degree': G_original.degree(node)} for node in G_original.nodes()]
    P_new = [{'degree': G_anonimised.degree(node)} for node in G_anonimised.nodes()]
    return np.sqrt(np.mean([(orig['degree'] - new['degree'])**2 for orig, new in zip(P_original, P_new)]))

def calculate_ilmae(G_original: nx.Graph, G_anonimised: nx.Graph) -> float:
    P_original = [{'degree': G_original.degree(node)} for node in G_original.nodes()]
    P_new = [{'degree': G_anonimised.degree(node)} for node in G_anonimised.nodes()]
    return np.sum([abs(orig['degree'] - new['degree']) for orig, new in zip(P_original, P_new)])

# Fonction pour compter les nœuds et arêtes d'un fichier .pairs
def count_nodes_edges(graph_file):
    nodes = set()
    edge_count = 0
    try:
        with open(graph_file, 'r') as f:
            for line in f:
                if line.strip():
                    edge = line.strip().split()
                    if len(edge) >= 2:
                        edge_count += 1
                        nodes.add(edge[0])
                        nodes.add(edge[1])
        return len(nodes), edge_count
    except Exception as e:
        print(f"Erreur lors de la lecture de {graph_file}: {e}")
        return 0, 0

# Parcourir tous les dossiers dans output
for dataset_name in os.listdir(OUTPUT_DIR):
    dataset_path = os.path.join(OUTPUT_DIR, dataset_name)
    print(f"Traitement du dataset : {dataset_name}")
    if not os.path.isdir(dataset_path):
        continue

    # Chemin du graphe original
    original_graph_path = os.path.join(DATA_DIR, f"{dataset_name}.pairs").replace(os.sep, '/')
    if not os.path.exists(original_graph_path):
        print(f"Graphe original {original_graph_path} non trouvé. Passage au suivant.")
        continue

    # Charger le graphe original
    G_original = load_graph_from_pairs(original_graph_path)

    # Calculer les nœuds et arêtes du graphe original
    original_nodes, original_edges = count_nodes_edges(original_graph_path)

    # Parcourir les dossiers d'algorithmes (ex. kDa, tMean, etc.)
    for algo in os.listdir(dataset_path):
        algo_path = os.path.join(dataset_path, algo)
        if not os.path.isdir(algo_path):
            continue

        # Fichier CSV de sortie pour ce dossier
        csv_file = os.path.join(algo_path, "utilite.csv").replace(os.sep, '/')
        csv_headers = ["k"] + UTILITY_METRICS + ["original_nodes", "original_edges", "anonymized_nodes", "anonymized_edges", "diff_nodes", "cc", "anon_cc", "ei", "apl", "anon_apl", "ilRMSE", "ilMAE"]

        # Initialiser le fichier CSV avec les en-têtes
        with open(csv_file, mode='w', newline='', encoding='utf-8') as f:
            writer = csv.writer(f)
            writer.writerow(csv_headers)

        # Parcourir les fichiers .pairs dans le dossier de l'algorithme
        for anon_file in os.listdir(algo_path):
            if not anon_file.endswith(".pairs"):
                continue

            # Extraire k du nom du fichier
            match = re.search(r'(\d+)\.pairs$', anon_file)
            if not match:
                continue
            k = int(match.group(1))

            # Chemin du graphe anonymisé
            anon_graph_path = os.path.join(algo_path, anon_file).replace(os.sep, '/')

            # Charger le graphe anonymisé
            G_anon = load_graph_from_pairs(anon_graph_path)

            # Calculer les nœuds et arêtes du graphe anonymisé
            anon_nodes, anon_edges = count_nodes_edges(anon_graph_path)

            # Calculer la différence entre le nombre de nœuds
            diff_nodes = original_nodes - anon_nodes

            # Calculer les métriques personnalisées
            print("calcul de cc")
            cc = calculate_clustering_coefficient(G_original)
            anon_cc = calculate_clustering_coefficient(G_anon)
            print("calcul de ei")
            ei = calculate_edge_intersection(G_original, G_anon)
            print("calcul de l'apl")
            apl = calculate_apl(G_original)
            anon_apl = calculate_apl(G_anon)
            print("calcul de l'information loss")
            ilrmse = calculate_ilrmse(G_original, G_anon)
            ilmae = calculate_ilmae(G_original, G_anon)

            # Calculer les métriques d'utilité de SecGraph
            metrics_results = {}
            for metric in UTILITY_METRICS:
                print(f"Calcul de la métrique {metric} pour {dataset_name}/{algo}/{anon_file}")
                command = [
                    "java", "-jar", SECGRAPH_JAR,
                    "-m", "u",
                    "-a", metric,
                    "-gA", anon_graph_path,
                    "-gB", original_graph_path
                ]

                try:
                    result = subprocess.run(command, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
                    metric_value = result.stdout.strip()
                    metrics_results[metric] = metric_value if metric_value else "N/A"
                except subprocess.CalledProcessError as e:
                    print(f"Erreur pour {metric} sur {anon_file}: {e.stderr}")
                    metrics_results[metric] = "N/A"

            # Écrire les résultats dans le CSV
            with open(csv_file, mode='a', newline='', encoding='utf-8') as f:
                writer = csv.writer(f)
                utilites = [k] + [metrics_results[metric] for metric in UTILITY_METRICS] + [original_nodes, original_edges, anon_nodes, anon_edges, diff_nodes, cc, anon_cc, ei, apl, anon_apl, ilrmse, ilmae]
                writer.writerow(utilites)

            print(f"Utilité calculée pour {dataset_name}/{algo}/{anon_file}")

print("Calcul des métriques d'utilité terminé.")