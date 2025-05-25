CREATE TABLE analysis_results (
    file_id UUID PRIMARY KEY,
    paragraphs_count INT NOT NULL,
    words_count INT NOT NULL,
    symbols_count INT NOT NULL,
    word_cloud_url TEXT
);