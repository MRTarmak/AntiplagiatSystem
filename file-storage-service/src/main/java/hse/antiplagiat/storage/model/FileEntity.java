package hse.antiplagiat.storage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.util.UUID;

@Entity
@Table(name = "files")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "hash", nullable = false, unique = true)
    private String hash;

    @Lob
    @JdbcTypeCode(Types.VARBINARY)
    @Column(name = "content", nullable = false)
    private byte[] content;
}