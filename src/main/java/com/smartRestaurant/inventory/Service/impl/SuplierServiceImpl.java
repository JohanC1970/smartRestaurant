package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.inventory.Repository.SuplierRepository;
import com.smartRestaurant.inventory.Service.SuplierService;
import com.smartRestaurant.inventory.dto.Suplier.CreateSuplierDTO;
import com.smartRestaurant.inventory.dto.Suplier.GetSuplierDTO;
import com.smartRestaurant.inventory.dto.Suplier.UpdateSuplierDTO;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.mapper.SuplierMapper;
import com.smartRestaurant.inventory.model.State;
import com.smartRestaurant.inventory.model.Suplier;
import io.jsonwebtoken.security.Jwks;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SuplierServiceImpl implements SuplierService {

    private final SuplierRepository suplierRepository;
    private final SuplierMapper suplierMapper;

    @Override
    public List<GetSuplierDTO> getAll() {
        return suplierRepository.findAll().stream().map(suplierMapper::toDto).toList();
    }

    @Override
    public void create(CreateSuplierDTO createSuplierDTO) {

        Optional<Suplier> suplier = suplierRepository.findByEmail(createSuplierDTO.email());

        if(suplier.isEmpty()){
            throw new ResourceNotFoundException("Suplier not found");
        }

        suplierRepository.save(suplierMapper.toEntity(createSuplierDTO));

    }

    @Override
    public void update(String id, UpdateSuplierDTO updateSuplierDTO) {

        Optional<Suplier> suplier = suplierRepository.findById(id);
        if(suplier.isEmpty()){
            throw new ResourceNotFoundException("Suplier not found");
        }

        suplierMapper.updateDto(updateSuplierDTO, suplier.get());
        suplierRepository.save(suplier.get());
    }

    @Override
    public void delete(String id) {
        Optional<Suplier> suplier = suplierRepository.findById(id);
        if(suplier.isEmpty()){
            throw new ResourceNotFoundException("Suplier not found");
        }

        suplier.get().setState(State.INACTIVE);

        suplierRepository.save(suplier.get());
    }

    @Override
    public GetSuplierDTO getById(String id) {
        Optional<Suplier> suplier = suplierRepository.findById(id);

        if(suplier.isEmpty()){
            throw new ResourceNotFoundException("Suplier not found");
        }

        return suplierMapper.toDto(suplier.get());
    }


}
