package com.project.hrms.service;

import com.project.hrms.dto.ShiftDTO;
import java.util.List;

public interface IShiftService {

    ShiftDTO createShift(ShiftDTO dto);

    List<ShiftDTO> getAllShifts();

    ShiftDTO getShiftById(Long id);

    ShiftDTO updateShift(Long id, ShiftDTO dto);

    void deleteShift(Long id);
}
