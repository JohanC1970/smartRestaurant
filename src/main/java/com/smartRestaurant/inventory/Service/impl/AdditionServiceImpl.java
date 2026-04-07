package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.inventory.Repository.AdditionRepository;
import com.smartRestaurant.inventory.Service.AdditionService;
import com.smartRestaurant.inventory.dto.Addition.CreateAdditionDTO;
import com.smartRestaurant.inventory.dto.Addition.GetAdditionDTO;
import com.smartRestaurant.inventory.dto.Addition.GetAdditionDetailDTO;
import com.smartRestaurant.inventory.dto.Addition.UpdateAdditionDTO;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.mapper.AdditionMapper;
import com.smartRestaurant.inventory.mapper.ShowAdditionDetailMapper;
import com.smartRestaurant.inventory.model.Addition;
import com.smartRestaurant.inventory.model.State;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdditionServiceImpl implements AdditionService {

    private final AdditionRepository additionRepository;
    private final AdditionMapper additionMapper;
    private final ShowAdditionDetailMapper showAdditionDetailMapper;

    @Override
    public List<GetAdditionDTO> getAll(int page) {

        Pageable pageable = PageRequest.of(page, 10);
        Page<Addition> additions = additionRepository.findAll(pageable);

        return additions.stream()
                .filter(addition -> addition.getState().equals(State.ACTIVE))
                .map(additionMapper::toDto)
                .toList();
    }

    @Transactional
    @Override
    public void create(CreateAdditionDTO createAdditionDTO) {
        Optional<Addition> addition = additionRepository.findByName(createAdditionDTO.name());
        if (addition.isPresent() && addition.get().getState().equals(State.ACTIVE)) {
            throw new RuntimeException("Already active addition");
        }
        additionRepository.save(additionMapper.toEntity(createAdditionDTO));
    }

    @Transactional
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

    @Transactional
    @Override
    public void delete(String id) {
        Optional<Addition> addition = additionRepository.findById(id);
        if (addition.isEmpty() || addition.get().getState().equals(State.INACTIVE)) {
            throw new RuntimeException("Addition does not exist");
        }
        addition.get().setState(State.INACTIVE);
        additionRepository.save(addition.get());

    }

    @Override
    public GetAdditionDetailDTO getById(String id) {

        Optional<Addition> addition = additionRepository.findById(id);
        if (addition.isEmpty() || addition.get().getState().equals(State.INACTIVE)) {
            throw new ResourceNotFoundException("Addition does not exist");
        }

        return showAdditionDetailMapper.toDTO(addition.get());
    }

}
