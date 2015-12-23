using Microsoft.AspNet.Identity;
using Microsoft.AspNet.Identity.EntityFramework;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using UniquePassword.Server.Models.Contexts;
using UniquePassword.Server.Models.Entities;
using UniquePassword.Server.Models.ViewModels;

namespace UniquePassword.Server.Models.Repositories
{
    public class AuthRepository : IDisposable
    {
        private readonly AuthContext _context;
        private readonly UserManager<IdentityUser> _userManager;

        public AuthRepository()
        {
            _context = new AuthContext();
            _userManager = new UserManager<IdentityUser>(new UserStore<IdentityUser>(_context));
        }

        public async Task<IdentityResult> RegisterUser(UserViewModel userModel)
        {
            var user = new IdentityUser()
            {
                Email = userModel.Username,
                UserName = userModel.Username,
            };

            return await _userManager.CreateAsync(user, userModel.Password);
        }

        public async Task<IdentityUser> FindUser(string userName, string password)
        {
            return await _userManager.FindAsync(userName, password);
        }

        public Client FindClient(string clientId)
        {
            return _context.Clients.Find(clientId);
        }

        public async Task<bool> AddRefreshToken(RefreshToken token)
        {
            var existingToken = _context.RefreshTokens.Where(r => r.Subject == token.Subject && r.ClientId == token.ClientId).SingleOrDefault();

            if (existingToken != null)
            {
                var result = await RemoveRefreshToken(existingToken);
            }

            _context.RefreshTokens.Add(token);

            return (await _context.SaveChangesAsync() > 0);
        }

        public async Task<bool> RemoveRefreshToken(string refreshTokenId)
        {
            var refreshToken = await _context.RefreshTokens.FindAsync(refreshTokenId);

            if (refreshToken != null)
            {
                _context.RefreshTokens.Remove(refreshToken);

                return (await _context.SaveChangesAsync() > 0);
            }

            return false;
        }

        public async Task<bool> RemoveRefreshToken(RefreshToken refreshToken)
        {
            _context.RefreshTokens.Remove(refreshToken);

            return (await _context.SaveChangesAsync() > 0);
        }

        public async Task<RefreshToken> FindRefreshToken(string refreshTokenId)
        {
            return await _context.RefreshTokens.FindAsync(refreshTokenId);
        }

        public IEnumerable<RefreshToken> GetAllRefreshTokens()
        {
            return _context.RefreshTokens.ToList();
        }

        public void Dispose()
        {
            _context.Dispose();
            _userManager.Dispose();
        }
    }
}
