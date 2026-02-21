package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.inventory.Repository.AdditionRepository;
import com.smartRestaurant.inventory.Service.AdditionService;
import com.smartRestaurant.inventory.dto.Addition.CreateAdditionDTO;
import com.smartRestaurant.inventory.dto.Addition.GetAdditionDTO;
import com.smartRestaurant.inventory.dto.Addition.UpdateAdditionDTO;
import com.smartRestaurant.inventory.mapper.AdditionMapper;
import com.smartRestaurant.inventory.model.Addition;
import com.smartRestaurant.inventory.model.State;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdditionServiceImpl implements AdditionService {

    private final AdditionRepository additionRepository;
    private final AdditionMapper additionMapper;

    @Override
    public List<GetAdditionDTO> getAll() {
        return additionRepository.findAll().stream().map(additionMapper::toDto).toList();
    }

    @Override
    public void create(CreateAdditionDTO createAdditionDTO) {
        Optional<Addition> addition = additionRepository.findByName(createAdditionDTO.name());
        if (addition.isPresent() && addition.get().getState().equals(State.ACTIVE)) {
            throw new RuntimeException("Already active addition");
        }
        additionRepository.save(additionMapper.toEntity(createAdditionDTO));
    }

    @Override
    public void update(String id, UpdateAdditionDTO updateAdditionDTO) {

        Optional<Addition> addition = additionRepository.findById(id);
        if (addition.isPresent() && addition.get().getState().equals(State.ACTIVE)) {
            additionMapper.update(updateAdditionDTO, addition.get());
            additionRepository.save(addition.get());
        }
        else  {
            throw new RuntimeException("Addition not found");
        }
    }

    @Override
    public void delete(String id) {
        Optional<Addition> addition = additionRepository.findById(id);
        if (addition.isEmpty() || addition.get().getState().equals(State.INACTIVE)) {
            throw new RuntimeException("Addition does not exist");
        }
        addition.get().setState(State.INACTIVE);
        additionRepository.save(addition.get());

    }
}
