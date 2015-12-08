using Android.App;
using Android.Views;
using Android.Widget;
using System.Collections.Generic;

namespace UniquePassword.AndroidApplication.Models
{
    public class KnownDomainAdapter : ArrayAdapter<KnownDomain>
    {
        private readonly Activity _context;
        private readonly List<KnownDomain> _objects;

        public KnownDomainAdapter(Activity context, List<KnownDomain> objects)
            : base(context, Android.Resource.Layout.TwoLineListItem, objects)
        {
            _context = context;
            _objects = objects;
        }

        public void Sort()
        {
            _objects.Sort();

            NotifyDataSetChanged();
        }

        public override View GetView(int position, View convertView, ViewGroup parent)
        {
            var view = convertView;
            var item = GetItem(position);

            if (view == null)
            {
                view = _context.LayoutInflater.Inflate(Android.Resource.Layout.TwoLineListItem, null);
            }

            var text1 = view.FindViewById<TextView>(Android.Resource.Id.Text1);
            var text2 = view.FindViewById<TextView>(Android.Resource.Id.Text2);

            if (item != null)
            {
                text1.Text = item.Domain;

                if (item.MaxLength.HasValue && item.SpecialCharacters != null)
                {
                    text2.Text = string.Format("Length: {0}, Extras: {1}", item.MaxLength.Value, item.SpecialCharacters);
                }
                else if (item.MaxLength.HasValue)
                {
                    text2.Text = string.Format("Length: {0}", item.MaxLength.Value);
                }
                else if (item.SpecialCharacters != null)
                {
                    text2.Text = string.Format("Extras: {0}", item.SpecialCharacters);
                }
                else
                {
                    text2.Text = "";
                }
            }

            return view;
        }
    }
}