package jp.levtech.rookie.attendance.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jp.levtech.rookie.attendance.model.TbMstEmployee;
import jp.levtech.rookie.attendance.repository.AttendanceRepository;
import jp.levtech.rookie.attendance.repository.TestRepository;

@Controller
public class HomeController {
			
	private final TestRepository testRepository;
	private final AttendanceRepository attendanceRepository;
		
	public HomeController (
			TestRepository testRepository,
			AttendanceRepository attendanceRepository) {
				
		this.testRepository = testRepository;
		this.attendanceRepository = attendanceRepository;
			
	}

	/**
	* ホーム画面を扱う。
	*
	* @return テンプレート
	*/
	@GetMapping("/home")
	public String home(Principal principal, Model model) {
		
		String employeeId = principal.getName();
		
		Optional<TbMstEmployee> employee =
				testRepository.findByEmployeeId(employeeId);
		
		model.addAttribute("employee", employee.get());
		
		LocalDate today = LocalDate.now();
		
		DateTimeFormatter formatter =
				DateTimeFormatter.ofPattern("yyyy年M月d日");
				
				model.addAttribute("today", today.format(formatter));
			// レンダリングに利用するテンプレート名を返す。
		return "home";
	}
	
	@PostMapping("/attendance/start")
		public String start(Principal principal) {
		
			String employeeId = principal.getName();
			
//			LocalDate workingDay = now Date();
//			LocalTime workingStartTime = LocalTime.now();
//			
//		
//			
//			attendanceRepository.insert(attendance);
			
			return "redirect:/home";
		
	}

}
