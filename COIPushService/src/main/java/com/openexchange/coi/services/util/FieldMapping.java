package com.openexchange.coi.services.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.coi.services.push.storage.PushResource;

public interface FieldMapping {

    int set(PreparedStatement stmt, int parameterIndex, PushResource object) throws SQLException;

}
