package com.project.hrms.service;

import com.project.hrms.dto.JobPositionDTO;
import com.project.hrms.model.JobPosition;
import com.project.hrms.response.JobPositionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

public interface IJobPositionService {


    JobPositionResponse create(JobPositionDTO jobPositionDTO);

    JobPositionResponse update(Long id, JobPositionDTO jobPositionDTO);

    void delete(Long id);

    JobPositionResponse getById(Long id);

    Page<JobPositionResponse> getAll(PageRequest pageRequest);

    Page<JobPositionResponse> search(String keyword, PageRequest pageRequest);

    ByteArrayInputStream exportToExcel(); // Export danh sách ra Excel


    List<JobPosition> getAllActive();

    Optional<JobPosition> getByCode(String code);


    // Kích hoạt một vị trí công việc.
    boolean activateJobPosition(Long id);

    //Vô hiệu hóa một vị trí công việc.
    boolean deactivateJobPosition(Long id);

    boolean existsByCode(String code);

    boolean existsById(Long id);

    /**
     * Tìm kiếm các vị trí công việc dựa trên các tiêu chí (ví dụ: tên, level, trạng thái).
     * Đây là một phương thức tổng quát, bạn có thể mở rộng thêm các tham số lọc.
     *
     * @param name     Tên vị trí công việc (có thể là null).
     * @param level    Cấp bậc vị trí công việc (có thể là null).
     * @param isActive Trạng thái hoạt động (có thể là null để tìm cả hai).
     * @return Danh sách các vị trí công việc thỏa mãn tiêu chí.
     */
    List<JobPosition> searchJobPositions(String name, String level, Boolean isActive);
}
