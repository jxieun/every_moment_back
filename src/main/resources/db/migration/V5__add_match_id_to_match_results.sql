ALTER TABLE match_results
ADD COLUMN match_id BIGINT,
ADD CONSTRAINT fk_match_result_match FOREIGN KEY (match_id) REFERENCES matches(id);