package com.jtspringproject.JtSpringProject.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.jtspringproject.JtSpringProject.models.Category;
import com.jtspringproject.JtSpringProject.models.Product;
import com.jtspringproject.JtSpringProject.models.User;
import com.jtspringproject.JtSpringProject.services.categoryService;
import com.jtspringproject.JtSpringProject.services.productService;
import com.jtspringproject.JtSpringProject.services.userService;


@Controller
public class UserController {

	private final userService userService;
	private final productService productService;
	private final categoryService categoryService;

	@GetMapping("/api/currentuser")
	@ResponseBody
	public ResponseEntity<?> getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
		}

		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("username", authentication.getName());
		return ResponseEntity.ok(userInfo);
	}

	@Autowired
	public UserController(userService userService, productService productService, categoryService categoryService) {
		this.userService = userService;
		this.productService = productService;
		this.categoryService = categoryService;
	}

	@GetMapping("/register")
	public String registerUser() {
		return "register";
	}

	@GetMapping("/buy")
	public String buy() {
		return "buy";
	}

	@GetMapping("/login")
	public ModelAndView userLogin(@RequestParam(required = false) String error) {
		ModelAndView mv = new ModelAndView("userLogin");
		if ("true".equals(error)) {
			mv.addObject("msg", "Please enter correct email and password");
		}
		return mv;
	}

	@GetMapping("/")
	public ModelAndView indexPage() {
		ModelAndView mView = new ModelAndView("index");
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		mView.addObject("username", username);
		List<Product> products = this.productService.getProducts();

		if (products.isEmpty()) {
			mView.addObject("msg", "No products are available");
		} else {
			mView.addObject("products", products);
		}
		return mView;
	}

	@GetMapping("/user/products")
	public ModelAndView getProducts() {

		ModelAndView mView = new ModelAndView("uproduct");

		List<Product> products = this.productService.getProducts();

		if (products.isEmpty()) {
			mView.addObject("msg", "No products are available");
		} else {
			mView.addObject("products", products);
		}

		return mView;
	}

	@PostMapping("newuserregister")
	@ResponseBody
	public ResponseEntity<String> registerNewUser(@ModelAttribute User user) {
		boolean exists = this.userService.checkUserExists(user.getUsername(), user.getEmail());

		if (!exists) {
			user.setRole("ROLE_NORMAL");
			this.userService.addUser(user);
			return ResponseEntity.ok("Registration successful");
		} else {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body("An account with this username and email already exists.");
		}
	}

	@GetMapping("/profileDisplay")
	public String profileDisplay(Model model) {

		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userService.getUserByUsername(username);

		if (user != null) {
			model.addAttribute("userid", user.getId());
			model.addAttribute("username", user.getUsername());
			model.addAttribute("email", user.getEmail());
			model.addAttribute("password", "");
			model.addAttribute("address", user.getAddress());
		} else {
			model.addAttribute("msg", "User not found");
		}

		return "updateProfile";
	}

	@PostMapping("/updateuser")
	public String updateUserProfile(@RequestParam("userid") int userid,
			@RequestParam("username") String username,
			@RequestParam("email") String email,
			@RequestParam("password") String password,
			@RequestParam("address") String address) {
		User updatedUser = this.userService.updateUserProfile(userid, username, email, password, address);
		if (updatedUser != null) {
			refreshAuthenticatedPrincipal(username);
		}
		return "redirect:/";
	}

	private void refreshAuthenticatedPrincipal(String username) {
		Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
		Authentication newAuthentication = new UsernamePasswordAuthenticationToken(
				username,
				currentAuthentication.getCredentials(),
				currentAuthentication.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(newAuthentication);
	}

	// ===================== JSON API for React customer site =====================

	@GetMapping("/api/products")
	@ResponseBody
	public ResponseEntity<List<Product>> getProductsApi() {
		List<Product> products = this.productService.getProducts();
		return ResponseEntity.ok(products);
	}

	@GetMapping("/api/products/{id}")
	@ResponseBody
	public ResponseEntity<Product> getProductByIdApi(@PathVariable("id") int id) {
		Product product = this.productService.getProduct(id);
		if (product == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(product);
	}

	@GetMapping("/api/categories")
	@ResponseBody
	public ResponseEntity<List<Category>> getCategoriesApi() {
		List<Category> categories = this.categoryService.getCategories();
		return ResponseEntity.ok(categories);
	}

}