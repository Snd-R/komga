CREATE INDEX idx__series__created_date on SERIES (CREATED_DATE);
CREATE INDEX idx__series__last_modified on SERIES (LAST_MODIFIED_DATE);

CREATE INDEX idx__read_progress__last_modified on READ_PROGRESS (LAST_MODIFIED_DATE);

CREATE INDEX idx__media__status on MEDIA (STATUS);
