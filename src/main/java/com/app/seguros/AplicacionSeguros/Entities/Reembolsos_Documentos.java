package com.app.seguros.AplicacionSeguros.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "reembolsos_documentos")
public class Reembolsos_Documentos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_reembolso_documento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id", nullable = false)
    @JsonIgnore
    private Documento documento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reembolso_id", nullable = false)
    @JsonIgnore
    private  Reembolso reembolso;


}
