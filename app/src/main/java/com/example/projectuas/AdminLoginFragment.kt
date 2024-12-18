package com.example.projectuas

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.projectuas.databinding.FragmentAdminLoginBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AdminLoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminLoginFragment : Fragment() {

    private var _binding: FragmentAdminLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminLoginBinding.inflate(inflater, container, false)

        // Inisialisasi SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)

        binding.btnLogin.setOnClickListener {
            val username = binding.usernameAdmin.text.toString().trim()
            val password = binding.passwordAdmin.text.toString().trim()

            // Periksa apakah input kosong
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Username dan password tidak boleh kosong.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simpan informasi login ke SharedPreferences tanpa validasi
            val editor = sharedPreferences.edit()
            editor.putBoolean("isLoggedIn", true)
            editor.putString("role", "admin") // Karena ini AdminLoginFragment, role otomatis 'admin'
            editor.putString("username", username) // Simpan username untuk keperluan lain
            editor.apply()

            // Navigasi ke AdminDashboardActivity
            val intent = Intent(activity, AdminDashboardActivity::class.java)
            startActivity(intent)
            activity?.finish() // Tutup fragment ini
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

