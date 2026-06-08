package com.safracerta.api.repository;

import com.safracerta.api.entity.LeituraSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeituraSensorRepository extends JpaRepository<LeituraSensor, Long> {
    List<LeituraSensor> findByTalhaoIdOrderByDataHoraDesc(Long talhaoId);

    @Modifying
    @Query("delete from LeituraSensor l where l.dispositivo.id = :dispositivoId")
    void deleteByDispositivoId(@Param("dispositivoId") Long dispositivoId);

    @Modifying
    @Query("delete from LeituraSensor l where l.talhao.id = :talhaoId")
    void deleteByTalhaoId(@Param("talhaoId") Long talhaoId);
}
