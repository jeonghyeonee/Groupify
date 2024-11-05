import pandas as pd
from sentence_transformers import SentenceTransformer
from sklearn.cluster import KMeans
from sklearn.feature_extraction.text import TfidfVectorizer
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.feature_extraction import text
import pickle

# 1. CSV 파일에서 데이터 불러오기
apps_data = pd.read_csv('cleaned_apps_data.csv')

# 2. 앱 이름을 불용어 리스트로 처리
app_names = apps_data['title'].apply(lambda x: x.lower()).tolist()

# 3. 추가적인 불용어 정의
additional_stopwords = ['pdf', 'file', 'app', 'lite', 'application']

# 4. 기본 불용어와 추가 불용어 결합
stop_words = list(text.ENGLISH_STOP_WORDS.union(app_names).union(additional_stopwords))

# 5. Sentence-BERT 모델 로드 및 문장 임베딩 생성
model = SentenceTransformer('all-MiniLM-L6-v2')
embeddings = model.encode(apps_data['description'].values)

# 6. K-Means 클러스터링 수행
n_clusters = 5  # 클러스터 개수는 필요에 따라 조정 가능
kmeans = KMeans(n_clusters=n_clusters, random_state=42)
clusters = kmeans.fit_predict(embeddings)

# KMeans 모델을 pkl 파일로 저장
with open('kmeans_model.pkl', 'wb') as file:
    pickle.dump(kmeans, file)

print("KMeans 모델이 'kmeans_model.pkl' 파일에 저장되었습니다.")

# 7. TF-IDF 기반 주요 키워드 추출
vectorizer = TfidfVectorizer(stop_words=stop_words, max_features=1000)
tfidf_matrix = vectorizer.fit_transform(apps_data['description'])

def get_top_keywords(tfidf_matrix, clusters, top_n=5):
    """
    클러스터별 상위 키워드를 추출하는 함수
    """
    cluster_centers = np.zeros((np.unique(clusters).size, tfidf_matrix.shape[1]))
    
    for cluster in np.unique(clusters):
        cluster_centers[cluster] = tfidf_matrix[clusters == cluster].mean(axis=0)
    
    terms = vectorizer.get_feature_names_out()
    top_keywords = []
    
    for cluster in range(cluster_centers.shape[0]):
        center = cluster_centers[cluster]
        top_indices = center.argsort()[::-1][:top_n]
        keywords = [terms[i] for i in top_indices]
        top_keywords.append(keywords)
    
    return top_keywords

# 8. 클러스터별 상위 5개 키워드 추출
top_keywords_per_cluster = get_top_keywords(tfidf_matrix, clusters, top_n=5)

# 9. 각 키워드를 임베딩하여 벡터 생성
def get_keyword_embedding(keywords):
    return model.encode(keywords)

# 10. Cosine Similarity를 사용하여 중심 단어 선택
def select_representative_word(keywords):
    keyword_embeddings = get_keyword_embedding(keywords)
    similarity_matrix = cosine_similarity(keyword_embeddings)

    # 각 단어의 유사도 합 계산
    similarity_sums = similarity_matrix.sum(axis=1)
    
    # 유사도 합이 가장 큰 단어를 대표 단어로 선택
    representative_idx = np.argmax(similarity_sums)
    return keywords[representative_idx]

# 11. 클러스터별 대표 단어 생성
def generate_category_name(keywords):
    return select_representative_word(keywords)

# 12. 클러스터별 대표 단어(카테고리 이름) 생성 및 결과 저장
cluster_data = []

for cluster_num, keywords in enumerate(top_keywords_per_cluster):
    category_name = generate_category_name(keywords)
    cluster_data.append({
        'Cluster': cluster_num,
        'Super Category': category_name,
        'Top Keywords': ', '.join(keywords)
    })

# DataFrame으로 변환
cluster_df = pd.DataFrame(cluster_data)

# 13. CSV로 저장
cluster_df.to_csv('super_categories.csv', index=False)

print("클러스터별 Super Category가 super_categories.csv 파일에 저장되었습니다.")
