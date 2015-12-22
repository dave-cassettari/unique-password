using Android.OS;
using Android.Support.V4.App;
using Android.Views;
using Java.Lang;
using System;
using System.Collections.Generic;

namespace UniquePassword.AndroidApplication.Support
{
    public class GenericFragmentPagerAdaptor : FragmentPagerAdapter
    {
        private readonly List<AddedFragment> _fragments = new List<AddedFragment>();

        public override int Count
        {
            get { return _fragments.Count; }
        }

        public GenericFragmentPagerAdaptor(FragmentManager fm)
            : base(fm)
        {

        }

        public override Fragment GetItem(int position)
        {
            if (position < 0 || position > Count - 1)
            {
                return null;
            }

            return _fragments[position].Fragment;
        }

        public override ICharSequence GetPageTitleFormatted(int position)
        {
            if (position < 0 || position > Count - 1)
            {
                return null;
            }

            var title = _fragments[position].Title;

            return new Java.Lang.String(title);
        }

        public void AddFragment(string title, GenericViewPagerFragment fragment)
        {
            var added = new AddedFragment(title, fragment);

            _fragments.Add(added);
        }

        public void AddFragmentView(string title, Func<LayoutInflater, ViewGroup, Bundle, View> view)
        {
            var added = new AddedFragment(title, new GenericViewPagerFragment(view));

            _fragments.Add(added);
        }

        private class AddedFragment
        {
            public string Title { get; private set; }
            public Fragment Fragment { get; private set; }

            public AddedFragment(string title, Fragment fragment)
            {
                Title = title;
                Fragment = fragment;
            }
        }
    }
}