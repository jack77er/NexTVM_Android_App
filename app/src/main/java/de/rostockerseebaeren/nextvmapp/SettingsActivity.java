package de.rostockerseebaeren.nextvmapp;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

            Preference rememberTime = findPreference("rememberTime");
            if(rememberTime != null) {
                final EditTextPreference txtPref = (EditTextPreference) rememberTime;

                String desc = getString(R.string.pref_remember_value_desc);
                String val = sharedPreferences.getString("rememberTime","");
                String out = String.format(desc,val);

                txtPref.setSummary(out);

                InputFilter[] filters = new InputFilter[1];
                filters[0] = new InputFilter() {
                    private int min = 0, max = 1440;

                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        try {
                            int input = Integer.parseInt(dest.toString() + source.toString());
                            if (isInRange(min, max, input))
                                return null;
                        } catch (NumberFormatException nfe) { }
                        return "";
                    }

                    private boolean isInRange(int a, int b, int c) {
                        return b > a ? c >= a && c <= b : c >= b && c <= a;
                    }
                };

                txtPref.getEditText().setFilters(filters);
                txtPref.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if(s.length() < 1) {
                            txtPref.getEditText().setError("Wert eingeben!");
                        }
                    }
                });

                txtPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if(newValue instanceof String) {
                            if (((String) newValue).length() > 0) {
                                try{
                                    Integer.parseInt(((String) newValue));
                                    return true;
                                } catch (NumberFormatException nex) {
                                    return false;
                                }
                            }
                        }
                        return false;
                    }
                });
            }

            Preference networkTimeout = findPreference("networkTimeout");
            if(networkTimeout != null) {
                final EditTextPreference txtPref = (EditTextPreference) networkTimeout;

                String desc = getString(R.string.pref_networkTimeout_desc);
                int val = Integer.parseInt(sharedPreferences.getString("networkTimeout","4000"));
                txtPref.setSummary(desc);

                InputFilter[] filters = new InputFilter[1];
                filters[0] = new InputFilter() {
                    private int min = 0, max = 60000;

                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        try {
                            int input = Integer.parseInt(dest.toString() + source.toString());
                            if (isInRange(min, max, input))
                                return null;
                        } catch (NumberFormatException nfe) { }
                        return "";
                    }

                    private boolean isInRange(int a, int b, int c) {
                        return b > a ? c >= a && c <= b : c >= b && c <= a;
                    }
                };

                txtPref.getEditText().setFilters(filters);
                txtPref.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if(s.length() < 1) {
                            txtPref.getEditText().setError("Wert eingeben!");
                        }
                    }
                });

                txtPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if(newValue instanceof String) {
                            if (((String) newValue).length() > 0) {
                                try{
                                    Integer.parseInt(((String) newValue));
                                    return true;
                                } catch (NumberFormatException nex) {
                                    return false;
                                }
                            }
                        }
                        return false;
                    }
                });
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(key.equals("rememberTime")) {
                Preference pref = findPreference(key);
                if (pref instanceof EditTextPreference) {
                    EditTextPreference txtPref = (EditTextPreference) pref;

                    String desc = getString(R.string.pref_remember_value_desc);
                    String val = sharedPreferences.getString("rememberTime","");
                    String out = String.format(desc,val);

                    txtPref.setSummary(out);
                }
            } else if(key.equals("networkTimeout")) {
                Preference pref = findPreference(key);
                if (pref instanceof EditTextPreference) {
                    EditTextPreference txtPref = (EditTextPreference) pref;

                    String desc = getString(R.string.pref_networkTimeout_desc);
                    int val = Integer.parseInt(sharedPreferences.getString("networkTimeout","4000"));
                    txtPref.setSummary(desc);
                }
            }
        }
    }
}