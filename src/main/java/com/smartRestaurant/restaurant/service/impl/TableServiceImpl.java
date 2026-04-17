package com.smartRestaurant.restaurant.service.impl;

import com.smartRestaurant.common.exception.ResourceNotFoundException;
import com.smartRestaurant.restaurant.dto.request.ChangeTableStatusDTO;
import com.smartRestaurant.restaurant.dto.request.CreateTableDTO;
import com.smartRestaurant.restaurant.dto.request.UpdateTableDTO;
import com.smartRestaurant.restaurant.dto.response.GetTableDTO;
import com.smartRestaurant.restaurant.model.RestaurantTable;
import com.smartRestaurant.restaurant.model.enums.TableStatus;
import com.smartRestaurant.restaurant.repository.TableRepository;
import com.smartRestaurant.restaurant.service.TableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TableServiceImpl implements TableService {

    private final TableRepository tableRepository;

    @Override
    @Transactional
    public GetTableDTO create(CreateTableDTO dto) {
        if (tableRepository.existsByNumber(dto.number())) {
            throw new IllegalArgumentException("Ya existe una mesa con el número " + dto.number());
        }

        RestaurantTable table = RestaurantTable.builder()
                .id(UUID.randomUUID().toString())
                .number(dto.number())
                .capacity(dto.capacity())
                .location(dto.location())
                .status(TableStatus.FREE)
                .active(true)
                .build();

        tableRepository.save(table);
        log.info("[TABLE] Mesa {} creada con ID {}", table.getNumber(), table.getId());
        return toDTO(table);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetTableDTO> getAll(TableStatus status) {
        List<RestaurantTable> tables = status != null
                ? tableRepository.findByStatusAndActiveTrue(status)
                : tableRepository.findByActiveTrue();

        return tables.stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GetTableDTO getById(String id) {
        return toDTO(findOrThrow(id));
    }

    @Override
    @Transactional
    public GetTableDTO update(String id, UpdateTableDTO dto) {
        RestaurantTable table = findOrThrow(id);

        if (dto.capacity() != null) {
            table.setCapacity(dto.capacity());
        }
        if (dto.location() != null) {
            table.setLocation(dto.location());
        }

        tableRepository.save(table);
        log.info("[TABLE] Mesa {} actualizada", table.getNumber());
        return toDTO(table);
    }

    @Override
    @Transactional
    public GetTableDTO changeStatus(String id, ChangeTableStatusDTO dto) {
        RestaurantTable table = findOrThrow(id);

        if (!table.isActive()) {
            throw new IllegalStateException("No se puede cambiar el estado de una mesa inactiva");
        }

        table.setStatus(dto.status());
        tableRepository.save(table);
        log.info("[TABLE] Mesa {} cambió a estado {}", table.getNumber(), dto.status());
        return toDTO(table);
    }

    @Override
    @Transactional
    public void deactivate(String id) {
        RestaurantTable table = findOrThrow(id);

        if (table.getStatus() == TableStatus.OCCUPIED) {
            throw new IllegalStateException("No se puede desactivar una mesa que está ocupada");
        }

        table.setActive(false);
        table.setStatus(TableStatus.FREE);
        tableRepository.save(table);
        log.info("[TABLE] Mesa {} desactivada", table.getNumber());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private RestaurantTable findOrThrow(String id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada con ID: " + id));
    }

    private GetTableDTO toDTO(RestaurantTable t) {
        return new GetTableDTO(t.getId(), t.getNumber(), t.getCapacity(),
                t.getStatus(), t.getLocation(), t.isActive());
    }
}
