from flask import Flask, request, jsonify
import pandas as pd
import pickle
from sklearn.feature_extraction.text import TfidfVectorizer
import numpy as np
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity
import os

from flask_cors import CORS

app = Flask(__name__)
CORS(app)


# Free_apps.csv 로드
def load_apps_data():
    try:
        df = pd.read_csv('Free_apps.csv')
        return df, None
    except FileNotFoundError:
        return None, "Free_apps.csv 파일을 찾을 수 없습니다."

# 모델 로드
def load_embeddings():
    try:
        with open('app_embeddings.pkl', 'rb') as file:
            return pickle.load(file), None
    except FileNotFoundError:
        return None, "app_embeddings.pkl 파일을 찾을 수 없습니다."

# 클러스터 모델 로드 함수
def load_kmeans_model(n_clusters):
    try:
        model_path = f'kmeans_model_{n_clusters}_clusters.pkl'
        if os.path.exists(model_path):
            with open(model_path, 'rb') as file:
                kmeans = pickle.load(file)
            return kmeans, None
        else:
            return None, f"{n_clusters}개의 클러스터를 사용하는 KMeans 모델을 찾을 수 없습니다."
    except Exception as e:
        return None, str(e)

# TF-IDF 기반으로 각 클러스터별 상위 키워드 추출
def extract_top_keywords(tfidf_matrix, clusters, vectorizer, top_n=5):
    num_clusters = np.unique(clusters).size
    cluster_centers = np.zeros((num_clusters, tfidf_matrix.shape[1]))

    for cluster in range(num_clusters):
        cluster_centers[cluster] = tfidf_matrix[clusters == cluster].mean(axis=0)
    
    terms = vectorizer.get_feature_names_out()
    top_keywords_per_cluster = []

    for cluster in range(cluster_centers.shape[0]):
        center = cluster_centers[cluster]
        top_indices = center.argsort()[::-1][:top_n]
        keywords = [terms[i] for i in top_indices]
        top_keywords_per_cluster.append(keywords)

    return top_keywords_per_cluster

# Cosine Similarity로 대표 키워드를 선택하는 함수
def select_representative_keyword(keywords):
    # SentenceTransformer 모델 로드
    model = SentenceTransformer('all-MiniLM-L6-v2')

    # 키워드 임베딩 생성
    keyword_embeddings = model.encode(keywords)

    # Cosine Similarity 계산
    similarity_matrix = cosine_similarity(keyword_embeddings)

    # 각 키워드의 유사도 합 계산
    similarity_sums = similarity_matrix.sum(axis=1)

    # 유사도 합이 가장 높은 키워드를 대표 키워드로 선택
    representative_idx = np.argmax(similarity_sums)
    return keywords[representative_idx]

# 클러스터링 API
@app.route('/cluster', methods=['POST'])
def cluster_apps():
    # 앱 임베딩 로드
    app_embeddings, err = load_embeddings()
    if app_embeddings is None:
        return jsonify({"error": err}), 500

    # 요청에서 클러스터 수 가져오기
    n_clusters = request.json.get('n_clusters')
    if not n_clusters or not isinstance(n_clusters, int):
        return jsonify({"error": "유효한 클러스터 수를 제공해야 합니다."}), 400

    # KMeans 모델 로드
    kmeans, err = load_kmeans_model(n_clusters)
    if kmeans is None:
        return jsonify({"error": err}), 404

    # 클러스터 예측
    clusters = kmeans.predict(app_embeddings)

    # Free_apps.csv 데이터를 로드
    df, err = load_apps_data()
    if df is None:
        return jsonify({"error": err}), 404

    # 클러스터 결과 추가
    df['Cluster'] = clusters

    # TF-IDF로 앱 설명을 분석하여 클러스터별로 상위 키워드 추출
    descriptions = df['Description'].values
    vectorizer = TfidfVectorizer(stop_words='english', max_features=1000)  # Vectorizer 선언
    tfidf_matrix = vectorizer.fit_transform(descriptions)  # TF-IDF 매트릭스 생성

    # 각 클러스터의 상위 키워드 추출
    top_keywords_per_cluster = extract_top_keywords(tfidf_matrix, clusters, vectorizer)

    # Cosine Similarity로 대표 키워드를 선택하여 카테고리 이름 생성
    cluster_names = [select_representative_keyword(keywords) for keywords in top_keywords_per_cluster]

    # 클러스터별로 그룹화하여 결과 반환
    grouped = df.groupby('Cluster').apply(lambda x: {
        "apps": x[['App ID', 'Name', 'Icon URL']].to_dict(orient='records'),
        "category_name": cluster_names[x.name]  # 각 클러스터의 카테고리 이름 추가
    }).to_dict()

    return jsonify(grouped)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
