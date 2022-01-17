package com.infusionsofgrandeur.lootraider.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager

import com.infusionsofgrandeur.lootraider.R

class ControlSchemeAdapter(private val context: Context) : RecyclerView.Adapter<ControlSchemeAdapter.ViewHolder>()
{

	/**
	 * Provide a reference to the type of views that you are using
	 * (custom ViewHolder).
	 */
	class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
	{
		val schemeThumbnailImageView: ImageView
		val currentSchemeTextView: TextView
		val schemeDescriptionTextView: TextView

		init
		{
			// Define click listener for the ViewHolder's View.
			schemeThumbnailImageView = view.findViewById(R.id.control_schemes_entry_image_view)
			currentSchemeTextView = view.findViewById(R.id.control_schemes_entry_current_scheme_text_view)
			schemeDescriptionTextView = view.findViewById(R.id.control_schemes_entry_scheme_description_text_view)
		}
	}

	// Create new views (invoked by the layout manager)
	override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder
	{
		// Create a new view, which defines the UI of the list item
		val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.control_scheme_entry, viewGroup, false)

		return ViewHolder(view)
	}

	// Replace the contents of a view (invoked by the layout manager)
	override fun onBindViewHolder(viewHolder: ViewHolder, position: Int)
	{

		val controlScheme = ConfigurationManager.currentLayoutScheme
		// Get element from your dataset at this position and replace the
		// contents of the view with that element
		var schemeDescription = ""
		var schemeThumbnail = context.resources.getDrawable(R.mipmap.screenshot_horizontal_1, null)
		viewHolder.currentSchemeTextView.visibility = View.INVISIBLE
		when (position)
		{
			0 -> {
				schemeDescription = context.resources.getString(R.string.control_scheme_landscape_control_right)
				schemeThumbnail = context.resources.getDrawable(R.mipmap.screenshot_horizontal_1, null)
				if (controlScheme == ConfigurationManager.GameScreenLayoutScheme.Horizontal1)
				{
					viewHolder.currentSchemeTextView.visibility = View.VISIBLE
				}
			}
			1 -> {
				schemeDescription = context.resources.getString(R.string.control_scheme_landscape_control_left)
				schemeThumbnail = context.resources.getDrawable(R.mipmap.screenshot_horizontal_2, null)
				if (controlScheme == ConfigurationManager.GameScreenLayoutScheme.Horizontal2)
				{
					viewHolder.currentSchemeTextView.visibility = View.VISIBLE
				}
			}
			2 -> {
				schemeDescription = context.resources.getString(R.string.control_scheme_landscape_control_dpad)
				schemeThumbnail = context.resources.getDrawable(R.mipmap.screenshot_horizontal_5, null)
				if (controlScheme == ConfigurationManager.GameScreenLayoutScheme.Horizontal5)
				{
					viewHolder.currentSchemeTextView.visibility = View.VISIBLE
				}
			}
			3 -> {
				schemeDescription = context.resources.getString(R.string.control_scheme_landscape_control_updown_left_leftright_right)
				schemeThumbnail = context.resources.getDrawable(R.mipmap.screenshot_horizontal_3, null)
				if (controlScheme == ConfigurationManager.GameScreenLayoutScheme.Horizontal3)
				{
					viewHolder.currentSchemeTextView.visibility = View.VISIBLE
				}
			}
			4 -> {
				schemeDescription = context.resources.getString(R.string.control_scheme_landscape_control_updown_right_leftright_left)
				schemeThumbnail = context.resources.getDrawable(R.mipmap.screenshot_horizontal_4, null)
				if (controlScheme == ConfigurationManager.GameScreenLayoutScheme.Horizontal4)
				{
					viewHolder.currentSchemeTextView.visibility = View.VISIBLE
				}
			}
			5 -> {
				schemeDescription = context.resources.getString(R.string.control_scheme_portrait)
				schemeThumbnail = context.resources.getDrawable(R.mipmap.screenshot_vertical, null)
				if (controlScheme == ConfigurationManager.GameScreenLayoutScheme.Vertical)
				{
					viewHolder.currentSchemeTextView.visibility = View.VISIBLE
				}
			}
		}
		viewHolder.schemeDescriptionTextView.text = schemeDescription
		viewHolder.schemeThumbnailImageView.setImageDrawable(schemeThumbnail)
	}

	// Return the size of your dataset (invoked by the layout manager)
	override fun getItemCount() = ConfigurationManager.defaultSettingsNumLayoutConfigurations

}
