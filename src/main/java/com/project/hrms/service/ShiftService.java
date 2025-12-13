package com.project.hrms.service;

import com.project.hrms.dto.ShiftDTO;
import com.project.hrms.exception.DataNotFoundException; // Từ file exception của bạn
import com.project.hrms.model.Shift;
import com.project.hrms.repository.ShiftRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper; // Bạn cần bean này từ MapperConfiguration
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftService implements IShiftService{

    private final ShiftRepository shiftRepository;
    private final ModelMapper modelMapper; // Inject từ MapperConfiguration

    // 1. Admin tạo ca mới
    public ShiftDTO createShift(ShiftDTO dto) {
        // (Có thể thêm logic kiểm tra "code" đã tồn tại chưa)
        Shift shift = modelMapper.map(dto, Shift.class);
        Shift savedShift = shiftRepository.save(shift);
        return modelMapper.map(savedShift, ShiftDTO.class);
    }

    // 2. Lấy tất cả các ca (cho Admin/Manager xem)
    public List<ShiftDTO> getAllShifts() {
        return shiftRepository.findAll().stream()
                .map(shift -> modelMapper.map(shift, ShiftDTO.class))
                .collect(Collectors.toList());
    }

    // 3. Lấy chi tiết 1 ca
    public ShiftDTO getShiftById(Long id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy ca làm việc."));
        return modelMapper.map(shift, ShiftDTO.class);
    }

    // 4. Admin cập nhật ca
    @Override // Nhớ thêm @Override nếu đang implement interface
    @Transactional // Nên có transactional khi update
    public ShiftDTO updateShift(Long id, ShiftDTO dto) {
        // 1. Tìm ca cũ trong DB
        Shift existingShift = shiftRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy ca làm việc."));

        // 2. LƯU LẠI CODE CŨ (Quan trọng)
        String oldCode = existingShift.getCode();

        // 3. Map dữ liệu mới vào (Lúc này code có thể bị ghi đè nếu dto.code khác null)
        modelMapper.map(dto, existingShift);

        // 4. KHÔI PHỤC LẠI CODE CŨ VÀ ID
        existingShift.setCode(oldCode); // Chặn việc sửa code
        existingShift.setShiftId(id);   // Đảm bảo ID không đổi

        // 5. Lưu và trả về
        Shift updatedShift = shiftRepository.save(existingShift);
        return modelMapper.map(updatedShift, ShiftDTO.class);
    }

    // 5. Admin xóa ca
    public void deleteShift(Long id) {
        if (!shiftRepository.existsById(id)) {
            throw new DataNotFoundException("Không tìm thấy ca làm việc.");
        }
        // (Nên kiểm tra xem ca này có đang được sử dụng ở
        // bảng shift_assignment không trước khi xóa)
        shiftRepository.deleteById(id);
    }
}