using Android.App;
using Android.Content;
using Android.OS;
using Android.Support.V4.App;
using Android.Support.V4.View;
using Android.Text;
using Android.Widget;
using System;
using System.Collections.Generic;
using System.Security.Cryptography;
using System.Text;
using UniquePassword.AndroidApplication.Models;
using UniquePassword.AndroidApplication.Support;

namespace UniquePassword.AndroidApplication
{
    [Activity(Label = "Unique Password", MainLauncher = true, Icon = "@drawable/icon")]
    public class MainActivity : FragmentActivity
    {
        public const int DefaultMaximumLength = 64;
        public const int MaximumRecentEntries = 5;
        public const int SpecialCharacterCount = 4;
        public const string DefaultSpecialCharacters = "!\"£$%^&*():@~<>?";

        private readonly List<KnownDomain> _knownDomains;

        private ListView _listRecent;
        private ViewPager _viewPager;
        private EditText _textMaster;
        private EditText _textDomain;
        private EditText _textHashed;
        private EditText _textLength;
        private EditText _textSpecials;
        private KnownDomainAdapter _listAdapter;

        public MainActivity()
        {
            _knownDomains = new List<KnownDomain>();

            _knownDomains.Add(new KnownDomain()
            {
                Domain = "www.facebook.com",
                MaxLength = 20,
                SpecialCharacters = null,
            });

            _knownDomains.Add(new KnownDomain()
            {
                Domain = "www.twitter.com",
                MaxLength = 30,
                SpecialCharacters = "!£$%^",
            });

            _knownDomains.Add(new KnownDomain()
            {
                Domain = "stackoverflow.com",
                MaxLength = null,
                SpecialCharacters = "ABC",
            });

            _knownDomains.Sort();
        }

        protected override void OnCreate(Bundle bundle)
        {
            base.OnCreate(bundle);

            SetContentView(Resource.Layout.Main);

            var fragmentAdaptor = new GenericFragmentPagerAdaptor(SupportFragmentManager);

            fragmentAdaptor.AddFragmentView("Recent", (inflater, viewGroup, fragmentBundle) =>
            {
                var fragment = inflater.Inflate(Resource.Layout.Recent, viewGroup, false);

                _listRecent = fragment.FindViewById<ListView>(Resource.Id.ListRecent);
                _listAdapter = new KnownDomainAdapter(this, _knownDomains);

                _listRecent.Adapter = _listAdapter;
                _listRecent.FastScrollEnabled = true;

                _listRecent.ItemClick += OnListViewItemClick;
                _listRecent.ItemLongClick += OnListRecentItemLongClick;

                _listAdapter.SetNotifyOnChange(true);

                return fragment;
            });

            fragmentAdaptor.AddFragmentView("Password", (inflater, viewGroup, fragmentBundle) =>
            {
                var fragment = inflater.Inflate(Resource.Layout.Data, viewGroup, false);

                _textMaster = fragment.FindViewById<EditText>(Resource.Id.TextMaster);
                _textDomain = fragment.FindViewById<EditText>(Resource.Id.TextDomain);
                _textHashed = fragment.FindViewById<EditText>(Resource.Id.TextHashed);

                _textMaster.TextChanged += OnInputChanged;
                _textDomain.TextChanged += OnInputChanged;

                var buttonCopy = fragment.FindViewById<Button>(Resource.Id.ButtonCopy);

                buttonCopy.Click += OnButtonCopyClick;

                return fragment;
            });

            fragmentAdaptor.AddFragmentView("Options", (inflater, viewGroup, fragmentBundle) =>
            {
                var fragment = inflater.Inflate(Resource.Layout.Options, viewGroup, false);

                _textLength = fragment.FindViewById<EditText>(Resource.Id.TextLength);
                _textSpecials = fragment.FindViewById<EditText>(Resource.Id.TextSpecials);

                _textLength.Text = DefaultMaximumLength.ToString();
                _textSpecials.Text = DefaultSpecialCharacters;

                _textLength.TextChanged += OnInputChanged;
                _textSpecials.TextChanged += OnInputChanged;

                return fragment;
            });

            _viewPager = FindViewById<ViewPager>(Resource.Id.ViewPager);

            _viewPager.Adapter = fragmentAdaptor;
            _viewPager.OffscreenPageLimit = 2;

            _viewPager.SetCurrentItem(1, false);

            _viewPager.PageSelected += OnViewPagerPageSelected;
        }

        private void Compute()
        {
            var master = _textMaster.Text;
            var domain = _textDomain.Text;
            var length = GetCurrentLength();
            var specials = new char[0];

            if (!string.IsNullOrEmpty(_textSpecials.Text))
            {
                specials = _textSpecials.Text.ToCharArray();
            }

            var computed = ComputeHash(master, domain, specials, length);

            _textHashed.Text = computed;
        }

        private void CopyAndStore()
        {
            if (string.IsNullOrEmpty(_textHashed.Text))
            {
                return;
            }

            var clipboardManager = GetSystemService(ClipboardService) as Android.Content.ClipboardManager;

            if (clipboardManager != null)
            {
                var clipData = ClipData.NewPlainText("Password", _textHashed.Text);

                clipboardManager.PrimaryClip = clipData;

                var toast = Toast.MakeText(this, "Password Copied", ToastLength.Short);

                RunOnUiThread(() => toast.Show());
            }

            SaveKnownDomain();
        }

        #region Hashing

        private string ComputeHash(string master, string domain, char[] specialCharacters, int? maxLength = null, int version = 1)
        {
            if (string.IsNullOrWhiteSpace(master) || string.IsNullOrWhiteSpace(domain))
            {
                return null;
            }

            var crypt = new SHA256Managed();
            var hashed = new StringBuilder();
            var encoding = Encoding.UTF8;
            var plainText = string.Format("{0}{1}{2}", master, domain, version);
            var encrypted = crypt.ComputeHash(encoding.GetBytes(plainText), 0, encoding.GetByteCount(plainText));

            foreach (var encryptedByte in encrypted)
            {
                hashed.Append(encryptedByte.ToString("x2"));
            }

            var password = hashed.ToString();

            if (maxLength.HasValue)
            {
                var maximimum = Math.Min(maxLength.Value, password.Length);

                password = password.Substring(0, maximimum);
            }

            if (specialCharacters != null && specialCharacters.Length > 0)
            {
                var hashedCode = 0;
                var newPassword = new StringBuilder(password);

                Array.Sort(specialCharacters, Comparer<char>.Default);

                for (var i = 0; i < password.Length; i++)
                {
                    hashedCode += (int)password[i];
                }

                var offset = hashedCode % specialCharacters.Length;
                var frequency = (int)Math.Floor((double)password.Length / SpecialCharacterCount);


                for (var i = 0; i < SpecialCharacterCount; i++)
                {
                    var includeChar = specialCharacters[(offset + i) % specialCharacters.Length];
                    var includeIndex = (offset + frequency * i) % password.Length;

                    newPassword[includeIndex] = includeChar;
                }

                password = newPassword.ToString();
            }

            //SaveSettings();

            return password;
        }

        #endregion

        #region Known Domains

        private void SaveKnownDomain()
        {
            RunOnUiThread(() =>
            {
                var knownDomain = new KnownDomain()
                {
                    Domain = _textDomain.Text,
                };
                var existingIndex = _knownDomains.IndexOf(knownDomain);
                var alreadyExisted = (existingIndex >= 0);

                if (alreadyExisted)
                {
                    knownDomain = _knownDomains[existingIndex];
                }

                var length = GetCurrentLength();
                var specials = _textSpecials.Text;

                if (length == DefaultMaximumLength)
                {
                    knownDomain.MaxLength = null;
                }
                else
                {
                    knownDomain.MaxLength = length;
                }

                if (specials == DefaultSpecialCharacters)
                {
                    knownDomain.SpecialCharacters = null;
                }
                else
                {
                    knownDomain.SpecialCharacters = specials;
                }

                if (!alreadyExisted)
                {
                    _listAdapter.Insert(knownDomain, 0);
                    _knownDomains.Insert(0, knownDomain);
                }

                _listAdapter.Sort();
                _knownDomains.Sort();
            });
        }

        private void LoadKnownDomain(KnownDomain knownDomain)
        {
            RunOnUiThread(() =>
            {
                if (knownDomain.MaxLength.HasValue)
                {
                    _textLength.Text = knownDomain.MaxLength.Value.ToString();
                }
                else
                {
                    _textLength.Text = DefaultMaximumLength.ToString();
                }

                if (knownDomain.SpecialCharacters != null)
                {
                    _textSpecials.Text = knownDomain.SpecialCharacters;
                }
                else
                {
                    _textSpecials.Text = DefaultSpecialCharacters;
                }

                _textDomain.Text = knownDomain.Domain;
                _textMaster.Text = null;

                _textMaster.RequestFocus();

                _viewPager.SetCurrentItem(1, true);
            });
        }

        private void RemoveKnownDomain(KnownDomain knownDomain)
        {
            RunOnUiThread(() =>
            {
                var alertBuilder = new AlertDialog.Builder(this);

                alertBuilder.SetTitle(string.Format("Remove {0}?", knownDomain.Domain));
                alertBuilder.SetPositiveButton("Yes", (alertSender, e) =>
                {
                    _listAdapter.Remove(knownDomain);
                    _knownDomains.Remove(knownDomain);
                });
                alertBuilder.SetNegativeButton("No", (alertSender, e) =>
                {

                });
                alertBuilder.SetIcon(Android.Resource.Drawable.IcDelete);
                alertBuilder.Show();
            });
        }

        #endregion

        #region Input Parsing

        private int GetCurrentLength()
        {
            int length;

            if (int.TryParse(_textLength.Text, out length))
            {
                return length;
            }

            return DefaultMaximumLength;
        }

        #endregion

        #region Event Handlers

        private void OnInputChanged(object sender, TextChangedEventArgs e)
        {
            Compute();
        }

        private void OnButtonCopyClick(object sender, EventArgs e)
        {
            CopyAndStore();
        }

        private void OnListViewItemClick(object sender, AdapterView.ItemClickEventArgs e)
        {
            var index = e.Position;

            if (index < 0 || index >= _knownDomains.Count)
            {
                return;
            }

            var knownDomain = _knownDomains[index];

            LoadKnownDomain(knownDomain);
        }

        private void OnListRecentItemLongClick(object sender, AdapterView.ItemLongClickEventArgs e)
        {
            var index = e.Position;

            if (index < 0 || index >= _knownDomains.Count)
            {
                return;
            }

            var knownDomain = _knownDomains[index];

            RemoveKnownDomain(knownDomain);
        }

        private void OnViewPagerPageSelected(object sender, ViewPager.PageSelectedEventArgs e)
        {
            switch (e.Position)
            {
                case 1:
                    _textMaster.RequestFocus();
                    break;

            }
        }

        #endregion
    }
}

