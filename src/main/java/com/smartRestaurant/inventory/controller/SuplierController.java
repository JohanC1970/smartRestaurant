package com.smartRestaurant.inventory.controller;

import com.smartRestaurant.inventory.Service.SuplierService;
import com.smartRestaurant.inventory.dto.Addition.CreateAdditionDTO;
import com.smartRestaurant.inventory.dto.Addition.GetAdditionDTO;
import com.smartRestaurant.inventory.dto.Addition.UpdateAdditionDTO;
import com.smartRestaurant.inventory.dto.ResponseDTO;
import com.smartRestaurant.inventory.dto.Suplier.CreateSuplierDTO;
import com.smartRestaurant.inventory.dto.Suplier.GetSuplierDTO;
import com.smartRestaurant.inventory.dto.Suplier.UpdateSuplierDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supliers")
@RequiredArgsConstructor
public class SuplierController {

    private final SuplierService suplierService;

    @GetMapping()
    @PreAuthorize("hasAnyAuthority('supplier:read', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<List<GetSuplierDTO>>> getAll(){
        List<GetSuplierDTO> list = suplierService.getAll();
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(list, false));
    }

    @PostMapping()
    @PreAuthorize("hasAnyAuthority('supplier:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> create(@Valid @RequestBody CreateSuplierDTO createSuplierDTO) {
        suplierService.create(createSuplierDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDTO<>("Proveedor creado", false));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('supplier:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> update(@PathVariable String id, @Valid @RequestBody UpdateSuplierDTO updateSuplierDTO) {
        suplierService.update(id, updateSuplierDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Proveedor actualizado", false));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('supplier:delete', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable String id){
        suplierService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Proveedor eliminado", false));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('supplier:read', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<GetSuplierDTO>> getById(@PathVariable String id){
        GetSuplierDTO suplierDTO = suplierService.getById(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(suplierDTO, false));
    }
}
